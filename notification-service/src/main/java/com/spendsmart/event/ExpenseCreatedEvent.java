package com.spendsmart.event; 

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCreatedEvent {
	private Integer userId;
	private String title;
	private BigDecimal amount;
}