package com.spendsmart.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringDto {
	private Integer recurringId;
	private Integer userId;
	private Integer categoryId;
	private String title;
	private BigDecimal amount;
	private String type; // "INCOME" or "EXPENSE"
	private String frequency; // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate nextDueDate;
	private Boolean isActive;
	private String paymentMethod;
	private String description;
}