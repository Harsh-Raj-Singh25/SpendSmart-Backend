package com.spendsmart.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

// ============================================================================
// PAYMENT ENTITY — Records every Razorpay payment attempt.
//
// LIFECYCLE:
// 1. User clicks "Upgrade to Premium" in the Angular frontend
// 2. Frontend calls POST /payments/create-order → we create a Razorpay Order
// 3. We save a Payment record with status = "CREATED"
// 4. Frontend opens Razorpay Checkout.js popup with this order
// 5. User completes payment in the Razorpay popup
// 6. Frontend receives razorpayPaymentId and razorpaySignature from Razorpay
// 7. Frontend calls POST /payments/verify → we verify the signature
// 8. If valid: status = "PAID", we call auth-service to upgrade to PREMIUM
// 9. If invalid: status = "FAILED"
//
// RAZORPAY TERMINOLOGY:
// - Order ID: Created by US on our server using Razorpay SDK
// - Payment ID: Created by RAZORPAY when the user actually pays
// - Signature: A hash that proves the payment is genuine (not forged)
// ============================================================================
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	// Which user made this payment — links to auth-service's users table
	@Column(nullable = false)
	private Integer userId;

	// Razorpay's order ID — created by our server via Razorpay SDK
	@Column(nullable = false, unique = true)
	private String razorpayOrderId;

	// Razorpay's payment ID — set after the user actually pays
	// null until payment is completed
	private String razorpayPaymentId;

	// Razorpay's signature — used to verify payment authenticity
	private String razorpaySignature;

	// Amount in the smallest currency unit (paise for INR)
	// 10000 paise = ₹100
	@Column(nullable = false)
	private Integer amount;

	@Column(length = 3, nullable = false)
	@Builder.Default
	private String currency = "INR";

	// Payment status: CREATED → PAID or FAILED
	@Column(nullable = false)
	@Builder.Default
	private String status = "CREATED";

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
}
