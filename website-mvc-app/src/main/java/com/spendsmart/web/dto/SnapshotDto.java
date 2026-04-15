package com.spendsmart.web.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SnapshotDto {
	private BigDecimal totalIncome;
	private BigDecimal totalExpenses;
	private BigDecimal netSavings;
	private BigDecimal savingsRate;
	private String topCategory;
}