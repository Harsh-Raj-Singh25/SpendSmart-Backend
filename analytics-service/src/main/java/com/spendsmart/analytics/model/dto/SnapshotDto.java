package com.spendsmart.analytics.model.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SnapshotDto {
	private BigDecimal totalIncome;
	private BigDecimal totalExpenses;
	private BigDecimal netSavings;
	private BigDecimal savingsRate;
	private String topCategory;
}
