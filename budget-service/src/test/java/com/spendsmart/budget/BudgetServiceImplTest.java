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

    @Test
    void createBudget_WhenNoActiveExisting_SavesBudget() {
        when(budgetRepository.findByUserIdAndCategoryId(1, 101)).thenReturn(Optional.empty());
        when(budgetRepository.save(mockBudget)).thenReturn(mockBudget);

        Budget created = budgetService.createBudget(mockBudget);

        assertEquals(1, created.getUserId());
        verify(budgetRepository).save(mockBudget);
    }

    @Test
    void createBudget_WhenActiveBudgetExists_ThrowsException() {
        when(budgetRepository.findByUserIdAndCategoryId(1, 101)).thenReturn(Optional.of(mockBudget));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> budgetService.createBudget(mockBudget));
        assertTrue(exception.getMessage().contains("active budget"));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_WhenExistingInactive_SavesBudget() {
        mockBudget.setIsActive(false);
        when(budgetRepository.findByUserIdAndCategoryId(1, 101)).thenReturn(Optional.of(mockBudget));
        when(budgetRepository.save(mockBudget)).thenReturn(mockBudget);

        Budget created = budgetService.createBudget(mockBudget);

        assertNotNull(created);
        verify(budgetRepository).save(mockBudget);
    }

    @Test
    void gettersAndDelete_DelegateToRepository() {
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));
        when(budgetRepository.findByUserId(1)).thenReturn(List.of(mockBudget));
        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of(mockBudget));
        when(budgetRepository.findByUserIdAndCategoryId(1, 101)).thenReturn(Optional.of(mockBudget));

        assertTrue(budgetService.getBudgetById(1).isPresent());
        assertEquals(1, budgetService.getBudgetsByUser(1).size());
        assertEquals(1, budgetService.getActiveBudgets(1).size());
        assertTrue(budgetService.getBudgetsByCategory(1, 101).isPresent());

        budgetService.deleteBudget(1);
        verify(budgetRepository).deleteByBudgetId(1);
    }

    @Test
    void updateBudget_Success_UpdatesFields() {
        Budget updates = Budget.builder()
                .name("Updated")
                .limitAmount(new BigDecimal("1500.00"))
                .period(BudgetPeriod.WEEKLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .alertThreshold(70)
                .isActive(false)
                .build();

        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget updated = budgetService.updateBudget(1, updates);

        assertEquals("Updated", updated.getName());
        assertEquals(new BigDecimal("1500.00"), updated.getLimitAmount());
        assertEquals(BudgetPeriod.WEEKLY, updated.getPeriod());
        assertFalse(updated.getIsActive());
    }

    @Test
    void updateSpentAmount_WhenTotalNegative_ClampsToZero() {
        mockBudget.setSpentAmount(new BigDecimal("20.00"));
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        budgetService.updateSpentAmount(1, new BigDecimal("-100.00"));

        assertEquals(BigDecimal.ZERO, mockBudget.getSpentAmount());
        verify(budgetRepository).save(mockBudget);
    }

    @Test
    void checkBudgetAlerts_WhenExceeded_ReturnsCriticalMessage() {
        mockBudget.setSpentAmount(new BigDecimal("1500.00"));
        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of(mockBudget));
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        List<String> alerts = budgetService.checkBudgetAlerts(1);

        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).contains("CRITICAL"));
    }

    @Test
    void checkBudgetAlerts_WhenSafe_ReturnsNoAlerts() {
        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of(mockBudget));
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(mockBudget));

        List<String> alerts = budgetService.checkBudgetAlerts(1);

        assertTrue(alerts.isEmpty());
    }

    @Test
    void resetBudgetPeriod_SetsSpentAmountToZero() {
        Budget second = Budget.builder()
                .budgetId(2)
                .userId(1)
                .categoryId(102)
                .name("Dining")
                .limitAmount(new BigDecimal("800.00"))
                .spentAmount(new BigDecimal("300.00"))
                .period(BudgetPeriod.MONTHLY)
                .alertThreshold(80)
                .isActive(true)
                .build();

        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of(mockBudget, second));

        budgetService.resetBudgetPeriod(1);

        assertEquals(BigDecimal.ZERO, mockBudget.getSpentAmount());
        assertEquals(BigDecimal.ZERO, second.getSpentAmount());
        verify(budgetRepository, times(2)).save(any(Budget.class));
    }

    @Test
    void getOverallBudgetAdherence_HandlesEmptyAndCapsOverspend() {
        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of());
        assertEquals(0.0, budgetService.getOverallBudgetAdherence(1));

        Budget overspent = Budget.builder()
                .budgetId(2)
                .userId(1)
                .categoryId(102)
                .name("Trips")
                .limitAmount(new BigDecimal("100.00"))
                .spentAmount(new BigDecimal("300.00"))
                .period(BudgetPeriod.MONTHLY)
                .alertThreshold(80)
                .isActive(true)
                .build();

        when(budgetRepository.findByUserIdAndIsActive(1, true)).thenReturn(List.of(mockBudget, overspent));

        double adherence = budgetService.getOverallBudgetAdherence(1);

        assertEquals(75.0, adherence, 0.001);
    }
}