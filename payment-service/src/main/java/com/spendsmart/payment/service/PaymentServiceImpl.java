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

import java.util.List;

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
@Service
@Slf4j
@Transactional
public class PaymentServiceImpl {

	private final PaymentRepository paymentRepository;
	private final AuthClient authClient;
	private final RazorpayClient razorpayClient;
	private final String keyId;
	private final String keySecret;
	private final int subscriptionAmount;
	private final String subscriptionCurrency;

	// Constructor injection — creates the RazorpayClient with our API keys
	public PaymentServiceImpl(
			PaymentRepository paymentRepository,
			AuthClient authClient,
			@Value("${razorpay.key-id}") String keyId,
			@Value("${razorpay.key-secret}") String keySecret,
			@Value("${subscription.amount}") int subscriptionAmount,
			@Value("${subscription.currency}") String subscriptionCurrency
	) throws Exception {
		this.paymentRepository = paymentRepository;
		this.authClient = authClient;
		this.keyId = keyId;
		this.keySecret = keySecret;
		this.subscriptionAmount = subscriptionAmount;
		this.subscriptionCurrency = subscriptionCurrency;
		// RazorpayClient is initialized once with our API credentials
		// All subsequent API calls (create order, etc.) use this client
		this.razorpayClient = new RazorpayClient(keyId, keySecret);
	}

	// ── STEP 1: CREATE RAZORPAY ORDER ───────────────────────────────────
	public CreateOrderResponse createOrder(CreateOrderRequest request) {
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
			throw new RuntimeException("Failed to create payment order: " + e.getMessage());
		}
	}

	// ── STEP 3: VERIFY RAZORPAY PAYMENT ─────────────────────────────────
	public String verifyPayment(VerifyPaymentRequest request) {
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
			authClient.upgradeToPremium(request.getUserId());
			log.info("User {} upgraded to PREMIUM", request.getUserId());

			return "Payment verified and subscription upgraded to PREMIUM!";

		} catch (RuntimeException e) {
			throw e; // Re-throw our own exceptions
		} catch (Exception e) {
			log.error("Payment verification error: {}", e.getMessage());
			throw new RuntimeException("Payment verification failed: " + e.getMessage());
		}
	}

	// ── GET PAYMENT HISTORY ─────────────────────────────────────────────
	public List<Payment> getPaymentsByUser(Integer userId) {
		return paymentRepository.findByUserId(userId);
	}

	// ── PRIVATE HELPER ──────────────────────────────────────────────────
	private void updatePaymentStatus(VerifyPaymentRequest request, String status) {
		Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
				.orElseThrow(() -> new RuntimeException("Payment not found for order: " + request.getRazorpayOrderId()));
		payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
		payment.setRazorpaySignature(request.getRazorpaySignature());
		payment.setStatus(status);
		paymentRepository.save(payment);
	}
}
