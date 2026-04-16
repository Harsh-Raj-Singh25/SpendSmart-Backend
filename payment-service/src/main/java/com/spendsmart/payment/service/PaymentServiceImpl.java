package com.spendsmart.payment.service;

import com.spendsmart.payment.client.AuthClient;
import com.spendsmart.payment.dto.*;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Locale;

// ============================================================================
// PAYMENT SERVICE — Handles Razorpay payment lifecycle.
//
// RAZORPAY PAYMENT FLOW (3 steps):
//
// STEP 1: CREATE ORDER (server-side)
//   - Our backend creates a Razorpay Order using the Razorpay SDK
//   - We get back an order_id from Razorpay
//   - We save it in our Payment table with status "CREATED"
//   - We return the order_id + amount + keyId to the frontend
//
// STEP 2: CHECKOUT (client-side — Angular)
//   - Frontend receives the order details
//   - Opens Razorpay Checkout.js popup with the order_id
//   - User enters card/UPI/net-banking details and pays
//   - Razorpay processes the payment and returns:
//     - razorpay_payment_id (proof of payment)
//     - razorpay_signature (HMAC to verify authenticity)
//
// STEP 3: VERIFY PAYMENT (server-side)
//   - Frontend sends payment_id + signature to our verify endpoint
//   - We compute HMAC(order_id|payment_id) using our secret key
//   - If computed signature matches received signature → GENUINE payment
//   - We update Payment status to "PAID"
//   - We call auth-service to upgrade user to PREMIUM (30 days)
//
// WHY SIGNATURE VERIFICATION?
//   Without it, anyone could fake a payment_id and upgrade for free.
//   The signature can only be generated using our secret key, which
//   only Razorpay and our server know.
// ============================================================================
// NOTE: @Transactional is NOT placed at class level.
// Each public method declares its own transaction boundary:
//
// - createOrder:   @Transactional(normal rollback) — safe to rollback on Razorpay API failure.
// - verifyPayment: @Transactional(noRollbackFor=RuntimeException) — the PAID status MUST
//   be committed even if the downstream auth-service Feign call throws. This prevents the
//   scenario where money is deducted from the user's account but our DB still shows CREATED.
@Service
@Slf4j
public class PaymentServiceImpl {

	private final PaymentRepository paymentRepository;
	private final AuthClient authClient;
	private final RazorpayClient razorpayClient;
	private final String keyId;
	private final String keySecret;
	private final int subscriptionAmount;
	private final String subscriptionCurrency;
	private final boolean mockMode;

	// Constructor injection — creates the RazorpayClient with our API keys
	public PaymentServiceImpl(
			PaymentRepository paymentRepository,
			AuthClient authClient,
			@Value("${razorpay.key-id}") String keyId,
			@Value("${razorpay.key-secret}") String keySecret,
			@Value("${subscription.amount}") int subscriptionAmount,
			@Value("${subscription.currency}") String subscriptionCurrency,
			@Value("${payment.mock-mode:false}") boolean mockMode
	) throws Exception {
		this.paymentRepository = paymentRepository;
		this.authClient = authClient;
		this.keyId = normalizeCredential(keyId);
		this.keySecret = normalizeCredential(keySecret);
		this.subscriptionAmount = subscriptionAmount;
		this.subscriptionCurrency = subscriptionCurrency;
		this.mockMode = mockMode;

		if (this.mockMode) {
			this.razorpayClient = null;
			log.warn("Payment service running in MOCK MODE. Razorpay API calls are bypassed.");
		} else {
			validateRazorpayCredentials(this.keyId, this.keySecret);
			// RazorpayClient is initialized once with our API credentials
			// All subsequent API calls (create order, etc.) use this client
			this.razorpayClient = new RazorpayClient(this.keyId, this.keySecret);
			log.info("Razorpay client initialized with key id prefix: {}", maskKeyId(this.keyId));
		}
	}

	// ── STEP 1: CREATE RAZORPAY ORDER ───────────────────────────────────
	// Normal @Transactional — if Razorpay API fails, payment record is NOT saved
	@Transactional
	public CreateOrderResponse createOrder(CreateOrderRequest request) {
		if (mockMode) {
			return createMockOrder(request);
		}

		try {
			log.info("Creating Razorpay order for user: {}", request.getUserId());

			// Build the order parameters for Razorpay API
			// amount is in paise (smallest currency unit): 10000 paise = ₹100
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", subscriptionAmount);
			orderRequest.put("currency", subscriptionCurrency);
			// receipt is our internal reference — useful for reconciliation
			orderRequest.put("receipt", "spendsmart_user_" + request.getUserId());

			// Call Razorpay API to create the order
			// This registers the order on Razorpay's servers
			Order razorpayOrder = razorpayClient.orders.create(orderRequest);
			String orderId = razorpayOrder.get("id");

			// Save the order in our database for tracking
			Payment payment = Payment.builder()
					.userId(request.getUserId())
					.razorpayOrderId(orderId)
					.amount(subscriptionAmount)
					.currency(subscriptionCurrency)
					.status("CREATED")
					.build();
			paymentRepository.save(payment);

			log.info("Razorpay order created: {} for user: {}", orderId, request.getUserId());

			// Return the order details to the frontend for checkout
			return new CreateOrderResponse(orderId, subscriptionAmount, subscriptionCurrency, keyId);

		} catch (Exception e) {
			log.error("Failed to create Razorpay order: {}", e.getMessage());
			if (e.getMessage() != null && e.getMessage().toLowerCase(Locale.ROOT).contains("authentication failed")) {
				throw new RuntimeException(
					"Failed to create payment order: Razorpay authentication failed. "
					+ "Verify RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET are a valid matching pair "
					+ "from the same mode (test or live)."
				);
			}
			throw new RuntimeException("Failed to create payment order: " + e.getMessage());
		}
	}

	// ── STEP 3: VERIFY RAZORPAY PAYMENT ─────────────────────────────────
	// noRollbackFor=RuntimeException: we ALWAYS want the status=PAID update committed
	// to the DB even if the auth-service Feign call later throws. This ensures we
	// have a payment audit trail and can manually replay the upgrade if needed.
	@Transactional(noRollbackFor = RuntimeException.class)
	public String verifyPayment(VerifyPaymentRequest request) {
		if (mockMode) {
			return verifyMockPayment(request);
		}

		try {
			log.info("Verifying Razorpay payment for order: {}", request.getRazorpayOrderId());

			// Step 3a: Verify the signature using Razorpay's utility method
			// This computes HMAC-SHA256(orderId|paymentId, keySecret) and
			// compares it with the received signature
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", request.getRazorpayOrderId());
			attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
			attributes.put("razorpay_signature", request.getRazorpaySignature());

			boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);

			if (!isValid) {
				log.warn("Payment signature verification FAILED for order: {}", request.getRazorpayOrderId());
				// Update payment status to FAILED
				updatePaymentStatus(request, "FAILED");
				throw new RuntimeException("Payment verification failed. Signature mismatch.");
			}

			// Step 3b: Signature is valid — payment is genuine!
			log.info("Payment verified successfully for order: {}", request.getRazorpayOrderId());
			updatePaymentStatus(request, "PAID");

			// Step 3c: Call auth-service to upgrade user to PREMIUM
			// This sets subscriptionType=PREMIUM and premiumExpiresAt=now+30days
			try {
				authClient.upgradeToPremium(request.getUserId());
				log.info("User {} upgraded to PREMIUM", request.getUserId());
			} catch (Exception feignEx) {
				// Payment is verified and recorded — but the upgrade call to auth-service
				// failed (e.g., auth-service is temporarily down or Eureka not ready).
				// We log a critical alert; an admin/retry job can replay upgrades from
				// the payments table where status=PAID but user is still FREE.
				log.error("CRITICAL: Payment PAID (order={}) but auth-service upgrade call FAILED for userId={}. Reason: {}",
						request.getRazorpayOrderId(), request.getUserId(), feignEx.getMessage());
				throw new RuntimeException(
					"Payment was successful but subscription upgrade failed. "
					+ "Please contact support with your order ID: " + request.getRazorpayOrderId());
			}

			return "Payment verified and subscription upgraded to PREMIUM!";

		} catch (RuntimeException e) {
			throw e; // Re-throw our own exceptions
		} catch (Exception e) {
			log.error("Payment verification error: {}", e.getMessage());
			throw new RuntimeException("Payment verification failed: " + e.getMessage());
		}
	}

	// ── GET PAYMENT HISTORY ─────────────────────────────────────────────
	@Transactional(readOnly = true)
	public List<Payment> getPaymentsByUser(Integer userId) {
		return paymentRepository.findByUserId(userId);
	}

	// ── PRIVATE HELPER ──────────────────────────────────────────────────
	private void updatePaymentStatus(VerifyPaymentRequest request, String status) {
		Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
				.orElseThrow(() -> new RuntimeException("Payment not found for order: " + request.getRazorpayOrderId()));
		payment.setRazorpayPaymentId(request.getRazorpayPaymentId() != null ? request.getRazorpayPaymentId() : "mock_payment_id");
		payment.setRazorpaySignature(request.getRazorpaySignature() != null ? request.getRazorpaySignature() : "mock_signature");
		payment.setStatus(status);
		paymentRepository.save(payment);
	}

	private CreateOrderResponse createMockOrder(CreateOrderRequest request) {
		String orderId = "order_mock_" + request.getUserId() + "_" + System.currentTimeMillis();

		Payment payment = Payment.builder()
				.userId(request.getUserId())
				.razorpayOrderId(orderId)
				.amount(subscriptionAmount)
				.currency(subscriptionCurrency)
				.status("CREATED")
				.build();
		paymentRepository.save(payment);

		log.info("Mock order created for user {}: {}", request.getUserId(), orderId);
		return new CreateOrderResponse(orderId, subscriptionAmount, subscriptionCurrency, "mock_key_id");
	}

	private String verifyMockPayment(VerifyPaymentRequest request) {
		log.info("Mock payment verification for order: {}", request.getRazorpayOrderId());
		updatePaymentStatus(request, "PAID");
		try {
			authClient.upgradeToPremium(request.getUserId());
			log.info("User {} upgraded to PREMIUM via mock payment mode", request.getUserId());
		} catch (Exception feignEx) {
			log.error("CRITICAL [MOCK MODE]: Auth-service upgrade call FAILED for userId={}. Reason: {}",
					request.getUserId(), feignEx.getMessage());
			throw new RuntimeException(
				"Mock payment recorded but subscription upgrade failed. "
				+ "Ensure auth-service is running and registered with Eureka. Order: " + request.getRazorpayOrderId());
		}
		return "Payment verified in mock mode and subscription upgraded to PREMIUM!";
	}

	private static String normalizeCredential(String value) {
		if (value == null) {
			return "";
		}
		String normalized = value.trim();
		if ((normalized.startsWith("\"") && normalized.endsWith("\""))
				|| (normalized.startsWith("'") && normalized.endsWith("'"))) {
			normalized = normalized.substring(1, normalized.length() - 1).trim();
		}
		return normalized;
	}

	private static void validateRazorpayCredentials(String keyId, String keySecret) {
		if (keyId.isBlank() || keySecret.isBlank()) {
			throw new IllegalStateException("Razorpay credentials are missing. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
		}
		if (keyId.contains("PLACEHOLDER") || keySecret.contains("PLACEHOLDER")) {
			throw new IllegalStateException("Razorpay credentials still use placeholders. Update .env with real keys.");
		}
	}

	private static String maskKeyId(String keyId) {
		if (keyId == null || keyId.isBlank()) {
			return "<empty>";
		}
		if (keyId.length() <= 6) {
			return keyId;
		}
		return keyId.substring(0, 6) + "...";
	}
}
