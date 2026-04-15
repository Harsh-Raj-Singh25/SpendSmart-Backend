package com.spendsmart.payment.dto;

import lombok.Data;

// ============================================================================
// DTO — Request body for creating a Razorpay Order.
// The Angular frontend sends this when the user clicks "Upgrade to Premium".
// ============================================================================
@Data
public class CreateOrderRequest {
	private Integer userId; // Which user wants to pay
}
