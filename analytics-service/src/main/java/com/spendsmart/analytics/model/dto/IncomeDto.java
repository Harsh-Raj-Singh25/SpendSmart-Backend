package com.spendsmart.analytics.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IncomeDto {
	private Integer incomeId;
	private Integer userId;
	private BigDecimal amount;
	private LocalDate date;
}
