package com.spendsmart.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCreatedEvent {
	private Integer userId;
	private String title;
	private BigDecimal amount;
}
