package com.spendsmart.budget;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.dto.BudgetProgress;
import com.spendsmart.budget.model.enums.BudgetPeriod;
import com.spendsmart.budget.repository.BudgetRepository;
import com.spendsmart.budget.service.BudgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Budget mockBudget;

    @BeforeEach
    void setUp() {
        mockBudget = Budget.builder()
                .budgetId(1)
                .userId(1)
                .categoryId(101)
                .name("Groceries")
                .limitAmount(new BigDecimal("1000.00"))
                .spentAmount(new BigDecimal("500.00"))
                .period(BudgetPeriod.MONTHLY)
                .startDate(LocalDate.now().withDayOfMonth(1))
                .endDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
                .alertThreshold(80)
                .isActive(true)
                .build();
    }

    @Test
    void getBudgetProgress_SafeStatus() {
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        BudgetProgress progress = budgetService.getBudgetProgress(1);

        assertEquals(new BigDecimal("500.00"), progress.getRemainingAmount());
        assertEquals(new BigDecimal("50.00"), progress.getPercentageUsed());
        assertEquals("SAFE", progress.getAlertStatus());
    }

    @Test
    void getBudgetProgress_WarningStatus() {
        mockBudget.setSpentAmount(new BigDecimal("850.00")); // 85% used
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        BudgetProgress progress = budgetService.getBudgetProgress(1);

        assertEquals(new BigDecimal("85.00"), progress.getPercentageUsed());
        assertEquals("WARNING", progress.getAlertStatus());
    }

    @Test
    void getBudgetProgress_ExceededStatus() {
        mockBudget.setSpentAmount(new BigDecimal("1100.00")); // 110% used
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        BudgetProgress progress = budgetService.getBudgetProgress(1);

        assertEquals("EXCEEDED", progress.getAlertStatus());
    }

    @Test
    void checkBudgetAlerts_ReturnsCorrectMessages() {
        mockBudget.setSpentAmount(new BigDecimal("900.00")); // 90%
        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of(mockBudget));
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        List<String> alerts = budgetService.checkBudgetAlerts(1);

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.get(0).contains("WARNING"));
        assertTrue(alerts.get(0).contains("90.00%"));
    }
}