package com.spendsmart.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

// ============================================================================
// DTO — Request body for creating a Razorpay Order.
// The Angular frontend sends this when the user clicks "Upgrade to Premium".
// ============================================================================
@Data
public class CreateOrderRequest {
	@NotNull(message = "User ID cannot be null")
	private Integer userId; // Which user wants to pay
}
