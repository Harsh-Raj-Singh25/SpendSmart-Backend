package com.spendsmart.expense.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BudgetServiceClientFallback implements BudgetServiceClient {

    @Override
    public void updateSpentAmountByCategory(Integer userId, Integer categoryId, BigDecimal amount) {
        log.warn(
                "BudgetServiceClient fallback triggered. Budget sync skipped for userId={}, categoryId={}, amount={}",
                userId, categoryId, amount);
    }
}
