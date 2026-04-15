package com.spendsmart.payment.resource;

import com.spendsmart.payment.dto.*;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.payment.service.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ============================================================================
// PAYMENT CONTROLLER — REST endpoints for Razorpay payment lifecycle.
//
// ANGULAR FRONTEND INTEGRATION:
//
// 1. User clicks "Upgrade to Premium" button
// 2. Angular calls: POST /payments/create-order { userId: 5 }
// 3. Gets back: { orderId: "order_xxx", amount: 10000, keyId: "rzp_test_xxx" }
// 4. Angular opens Razorpay popup:
//      const rzp = new Razorpay({
//        key: response.keyId,
//        amount: response.amount,
//        order_id: response.orderId,
//        handler: (rzpResponse) => {
//          // Call verify endpoint
//          http.post('/payments/verify', {
//            razorpayOrderId: rzpResponse.razorpay_order_id,
//            razorpayPaymentId: rzpResponse.razorpay_payment_id,
//            razorpaySignature: rzpResponse.razorpay_signature,
//            userId: 5
//          })
//        }
//      });
//      rzp.open();
// 5. After verification succeeds, user is upgraded to PREMIUM
// ============================================================================
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentResource {

	private final PaymentServiceImpl paymentService;

	// ── Step 1: Create a Razorpay Order ─────────────────────────────────
	// Called when user clicks "Upgrade to Premium"
	// Returns order details that the frontend uses to open Razorpay Checkout
	@PostMapping("/create-order")
	public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
		log.info("POST /payments/create-order for user: {}", request.getUserId());
		return ResponseEntity.ok(paymentService.createOrder(request));
	}

	// ── Step 3: Verify the Razorpay Payment ─────────────────────────────
	// Called after user completes payment in Razorpay popup
	// Verifies signature, updates payment status, upgrades subscription
	@PostMapping("/verify")
	public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody VerifyPaymentRequest request) {
		log.info("POST /payments/verify for order: {}", request.getRazorpayOrderId());
		String message = paymentService.verifyPayment(request);
		return ResponseEntity.ok(Map.of("message", message));
	}

	// ── Payment History ─────────────────────────────────────────────────
	// Returns all payment records for a user (for the profile/settings page)
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Integer userId) {
		return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
	}
}
