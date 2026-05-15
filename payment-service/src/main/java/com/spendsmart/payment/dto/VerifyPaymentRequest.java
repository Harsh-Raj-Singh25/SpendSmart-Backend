package com.spendsmart.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
	@NotBlank(message = "Razorpay order ID cannot be blank")
	private String razorpayOrderId;     // The order we created
	
	@NotBlank(message = "Razorpay payment ID cannot be blank")
	private String razorpayPaymentId;   // Razorpay's payment confirmation ID
	
	@NotBlank(message = "Razorpay signature cannot be blank")
	private String razorpaySignature;   // HMAC signature to verify authenticity
	
	@NotNull(message = "User ID cannot be null")
	private Integer userId;             // Which user made the payment
}
