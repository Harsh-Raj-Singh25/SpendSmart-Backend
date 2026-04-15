package com.spendsmart.web.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BudgetDto {
	private Integer budgetId;
	private Integer userId;
	private Integer categoryId;
	private BigDecimal budgetLimit;
	private int month;
	private int year;
}