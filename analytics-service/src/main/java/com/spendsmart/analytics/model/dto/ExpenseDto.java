package com.spendsmart.analytics.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDto {
	private Integer expenseId;
	private Integer userId;
	private Integer categoryId;
	private String title;
	private BigDecimal amount;
	private LocalDate date;
}