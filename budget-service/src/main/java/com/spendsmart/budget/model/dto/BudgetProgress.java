package com.spendsmart.budget.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetProgress {
	private Integer budgetId;
	private BigDecimal limitAmount;
	private BigDecimal spentAmount;
	private BigDecimal remainingAmount;
	private BigDecimal percentageUsed;
	private String alertStatus; // e.g., "SAFE", "WARNING", "EXCEEDED"
}