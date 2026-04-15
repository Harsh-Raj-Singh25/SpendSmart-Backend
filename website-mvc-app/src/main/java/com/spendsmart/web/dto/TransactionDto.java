package com.spendsmart.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDto {
    private Integer id; // Maps to expenseId or incomeId
    private Integer userId;
    private Integer categoryId;
    private String title;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDate date;
    private String type; // "INCOME" or "EXPENSE"
}