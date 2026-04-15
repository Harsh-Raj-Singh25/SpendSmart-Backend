package com.spendsmart.expense.client;

import org.springframework.cloud.openfeign.FeignClient; 
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

// Tell Feign exactly where the Budget Service lives
@FeignClient(name = "budget-service")
public interface BudgetServiceClient {

    // This perfectly matches the endpoint we just created in Step 1!
    @PutMapping("/budgets/user/{userId}/category/{categoryId}/spent")
    void updateSpentAmountByCategory(
            @PathVariable("userId") Integer userId,
            @PathVariable("categoryId") Integer categoryId,
            @RequestParam("amount") BigDecimal amount
    );
}