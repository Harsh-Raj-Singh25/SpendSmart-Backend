package com.spendsmart.web;

import com.spendsmart.web.clients.*;
import com.spendsmart.web.controller.AnalyticsController;
import com.spendsmart.web.dto.BudgetDto;
import com.spendsmart.web.dto.RecurringDto;
import com.spendsmart.web.dto.SnapshotDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsClient analyticsClient;

    @Mock
    private ExpenseClient expenseClient;

    @Mock
    private IncomeClient incomeClient;

    @Mock
    private BudgetClient budgetClient;

    @Mock
    private RecurringClient recurringClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AnalyticsController analyticsController;

    @Test
    void viewAndApiEndpointsDelegateToClients() {
        assertEquals("charts-dashboard", analyticsController.viewCharts().getViewName());

        BudgetDto budgetDto = new BudgetDto();
        when(budgetClient.getUserBudgets(5)).thenReturn(List.of(budgetDto));
        assertEquals("budgets", analyticsController.viewBudgets().getViewName());

        BudgetDto newBudget = new BudgetDto();
        assertEquals("redirect:/app/analytics/budgets", analyticsController.addBudget(newBudget));
        ArgumentCaptor<BudgetDto> budgetCaptor = ArgumentCaptor.forClass(BudgetDto.class);
        verify(budgetClient).addBudget(budgetCaptor.capture());
        assertEquals(5, budgetCaptor.getValue().getUserId());

        RecurringDto recurringDto = new RecurringDto();
        when(recurringClient.getActiveRecurringTransactions(5)).thenReturn(List.of(recurringDto));
        assertEquals("recurring-transactions", analyticsController.viewRecurring().getViewName());

        RecurringDto newRecurring = new RecurringDto();
        assertEquals("redirect:/app/analytics/recurring", analyticsController.addRecurring(newRecurring));
        ArgumentCaptor<RecurringDto> recurringCaptor = ArgumentCaptor.forClass(RecurringDto.class);
        verify(recurringClient).addRecurringTransaction(recurringCaptor.capture());
        assertEquals(5, recurringCaptor.getValue().getUserId());

        SnapshotDto snapshotDto = new SnapshotDto();
        snapshotDto.setTopCategory("Food");
        when(analyticsClient.getMonthlySnapshot(5, 4, 2026)).thenReturn(snapshotDto);
        assertEquals("Food", analyticsController.getMonthlySummary(4, 2026).getBody().getTopCategory());

        when(analyticsClient.getCategoryPieChart(5, 4, 2026)).thenReturn(Map.of("Food", 75.0));
        assertEquals(75.0, analyticsController.getCategoryPieChart(4, 2026).getBody().get("Food"));

        when(analyticsClient.getCashflowData(5, 4, 2026)).thenReturn(Map.of("months", List.of("Jan")));
        assertEquals(1, ((List<?>) analyticsController.getCashflowData(4, 2026).getBody().get("months")).size());

        when(analyticsClient.getFinancialHealthScore(5, 4)).thenReturn(82);
        assertEquals(82, analyticsController.getHealthScore(4).getBody());
    }
}
