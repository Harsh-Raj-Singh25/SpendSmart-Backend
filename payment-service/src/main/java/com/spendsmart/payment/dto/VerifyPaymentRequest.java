package com.spendsmart.payment.dto;

import lombok.Data;

// ============================================================================
// DTO — Request body for verifying a Razorpay payment.
// After the user completes payment in the Razorpay popup, the frontend
// receives these three values from Razorpay and sends them here for verification.
//
// VERIFICATION PROCESS:
// 1. We compute: SHA256_HMAC(orderId + "|" + paymentId, razorpayKeySecret)
// 2. We compare our computed signature with the received signature
// 3. If they match → payment is genuine → upgrade user to PREMIUM
// 4. If they don't match → payment is forged → reject
// ============================================================================
@Data
public class VerifyPaymentRequest {
	private String razorpayOrderId;     // The order we created
	private String razorpayPaymentId;   // Razorpay's payment confirmation ID
	private String razorpaySignature;   // HMAC signature to verify authenticity
	private Integer userId;             // Which user made the payment
}
