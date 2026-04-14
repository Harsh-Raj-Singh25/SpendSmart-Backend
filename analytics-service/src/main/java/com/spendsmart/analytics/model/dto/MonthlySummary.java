package com.spendsmart.analytics.model.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlySummary {
	private int year;
	private int month;
	private BigDecimal totalIncome;
	private BigDecimal totalExpense;
	private BigDecimal netSavings;
	private String topCategory;
	
	
}
