package com.spendsmart.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// ============================================================================
// DTO — Response after creating a Razorpay Order.
// The Angular frontend uses these values to open the Razorpay Checkout popup.
//
// FRONTEND USAGE (Angular):
//   const options = {
//     key: response.keyId,           // Razorpay Key ID
//     amount: response.amount,       // Amount in paise
//     currency: response.currency,
//     order_id: response.orderId,    // Our server-created order
//     handler: function(response) {  // Called after payment success
//       // POST /payments/verify with razorpayPaymentId + razorpaySignature
//     }
//   };
//   new Razorpay(options).open();
// ============================================================================
@Data
@AllArgsConstructor
public class CreateOrderResponse {
	private String orderId;   // Razorpay Order ID (starts with "order_")
	private Integer amount;   // Amount in paise (10000 = ₹100)
	private String currency;  // "INR"
	private String keyId;     // Razorpay Key ID — frontend needs this to open checkout
}
