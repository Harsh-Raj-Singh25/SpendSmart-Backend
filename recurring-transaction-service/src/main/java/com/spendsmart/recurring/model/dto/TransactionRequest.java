package com.spendsmart.recurring.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRequest {
	private Integer userId;
	private Integer categoryId;
	private String title;
	private BigDecimal amount;
	private String currency;
	private String type;   //Either "INCOME" or "EXPENSE"
	private String paymentMethod; //e.g., "UPI", "CASH"
	private LocalDate date;
	private Boolean isRecurring;
	private String notes;
}
