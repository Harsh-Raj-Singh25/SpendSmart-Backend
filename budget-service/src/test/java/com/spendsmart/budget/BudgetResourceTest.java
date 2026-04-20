package com.spendsmart.budget;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.dto.BudgetProgress;
import com.spendsmart.budget.resource.BudgetResource;
import com.spendsmart.budget.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetResourceTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetResource budgetResource;

    @Test
    void allEndpointsDelegateToService() {
        Budget budget = Budget.builder()
                .budgetId(1)
                .userId(5)
                .categoryId(2)
                .name("Monthly Food")
                .limitAmount(new BigDecimal("12000.00"))
                .build();

        when(budgetService.createBudget(budget)).thenReturn(budget);
        assertEquals(201, budgetResource.createBudget(budget).getStatusCode().value());

        when(budgetService.getBudgetById(1)).thenReturn(Optional.of(budget));
        assertEquals(200, budgetResource.getById(1).getStatusCode().value());

        when(budgetService.getBudgetById(99)).thenReturn(Optional.empty());
        assertEquals(404, budgetResource.getById(99).getStatusCode().value());

        when(budgetService.getBudgetsByUser(5)).thenReturn(List.of(budget));
        assertEquals(1, budgetResource.getByUser(5).getBody().size());

        when(budgetService.getActiveBudgets(5)).thenReturn(List.of(budget));
        assertEquals(1, budgetResource.getActive(5).getBody().size());

        when(budgetService.updateBudget(1, budget)).thenReturn(budget);
        assertEquals(1, budgetResource.update(1, budget).getBody().getBudgetId());

        assertEquals(204, budgetResource.delete(1).getStatusCode().value());
        verify(budgetService).deleteBudget(1);

        assertEquals(200, budgetResource.updateSpentAmount(1, new BigDecimal("250.00")).getStatusCode().value());
        verify(budgetService).updateSpentAmount(1, new BigDecimal("250.00"));

        BudgetProgress progress = BudgetProgress.builder().limitAmount(new BigDecimal("1000.00")).spentAmount(new BigDecimal("300.00")).build();
        when(budgetService.getBudgetProgress(1)).thenReturn(progress);
        assertEquals(new BigDecimal("1000.00"), budgetResource.getProgress(1).getBody().getLimitAmount());

        when(budgetService.checkBudgetAlerts(5)).thenReturn(List.of("80% used"));
        assertEquals(1, budgetResource.getAlerts(5).getBody().size());

        assertEquals(200, budgetResource.resetPeriod(5).getStatusCode().value());
        verify(budgetService).resetBudgetPeriod(5);

        when(budgetService.getBudgetsByCategory(5, 2)).thenReturn(Optional.of(budget));
        assertEquals(200, budgetResource.updateSpentAmountByCategory(5, 2, new BigDecimal("120.00")).getStatusCode().value());
        verify(budgetService).updateSpentAmount(1, new BigDecimal("120.00"));

        when(budgetService.getOverallBudgetAdherence(5)).thenReturn(87.5);
        assertEquals(87.5, budgetResource.getOverallBudgetAdherence(5).getBody());
    }

    @Test
    void updateSpentAmountByCategory_NoBudgetFound_DoesNotUpdate() {
        when(budgetService.getBudgetsByCategory(7, 55)).thenReturn(Optional.empty());

        assertEquals(200, budgetResource.updateSpentAmountByCategory(7, 55, new BigDecimal("80.00")).getStatusCode().value());

        verify(budgetService, never()).updateSpentAmount(anyInt(), any(BigDecimal.class));
    }
}
