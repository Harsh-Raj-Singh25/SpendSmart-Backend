package com.spendsmart.web.clients;

import com.spendsmart.web.dto.BudgetDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "budget-client", url = "http://localhost:8080/budgets")
public interface BudgetClient {

    @GetMapping("/user/{userId}")
    List<BudgetDto> getUserBudgets(@PathVariable("userId") int userId);

    @PostMapping
    BudgetDto addBudget(@RequestBody BudgetDto budget);

    @PutMapping("/{budgetId}")
    BudgetDto updateBudget(@PathVariable("budgetId") int budgetId, @RequestBody BudgetDto budget);

    @DeleteMapping("/{budgetId}")
    void deleteBudget(@PathVariable("budgetId") int budgetId);
}