package com.spendsmart.analytics;

import com.spendsmart.analytics.client.BudgetClient;
import com.spendsmart.analytics.client.ExpenseClient;
import com.spendsmart.analytics.client.IncomeClient;
import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.model.dto.ExpenseDto;
import com.spendsmart.analytics.model.dto.MonthlySummary;
import com.spendsmart.analytics.model.dto.SnapshotDto;
import com.spendsmart.analytics.model.dto.YearlySummary;
import com.spendsmart.analytics.repository.AnalyticsRepository;
import com.spendsmart.analytics.service.AnalyticsServiceImpl; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    
    @Mock
    private IncomeClient incomeClient;
    
    @Mock
    private ExpenseClient expenseClient;
    
    @Mock
    private BudgetClient budgetClient;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void generateMonthlySnapshot_Success() {
        // Arrange: Mock the Feign Client Network Calls
        when(incomeClient.getTotalIncomeByMonth(5, 2026, 4)).thenReturn(new BigDecimal("5000.00"));
        when(expenseClient.getTotalExpenseByMonth(5, 2026, 4)).thenReturn(new BigDecimal("2000.00"));
        
        Map<String, BigDecimal> breakdown = new HashMap<>();
        breakdown.put("15", new BigDecimal("2000.00")); // Category 15 is the highest
        when(expenseClient.getExpenseBreakdownByCategory(5, 2026, 4)).thenReturn(breakdown);

        // Arrange: Mock the Database
        when(analyticsRepository.findByUserIdAndYearAndMonth(5, 2026, 4)).thenReturn(Optional.empty());
        when(analyticsRepository.save(any(FinancialSnapshot.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        FinancialSnapshot snapshot = analyticsService.generateMonthlySnapshot(5, 2026, 4);

        // Assert
        assertNotNull(snapshot);
        assertEquals(new BigDecimal("5000.00"), snapshot.getTotalIncome());
        assertEquals(new BigDecimal("2000.00"), snapshot.getTotalExpenses());
        assertEquals(new BigDecimal("3000.00"), snapshot.getNetSavings());
        // Savings rate = (3000/5000) * 100 = 60.00%
        assertEquals(new BigDecimal("60.00"), snapshot.getSavingsRate());
        assertEquals("15", snapshot.getTopCategory());
        
        verify(analyticsRepository, times(1)).save(any(FinancialSnapshot.class));
    }

    @Test
    void generateMonthlySnapshot_UpdatesExistingSnapshot() {
        FinancialSnapshot existing = FinancialSnapshot.builder()
                .snapshotId(99)
                .userId(5)
                .year(2026)
                .month(4)
                .period("OLD")
                .build();

        when(incomeClient.getTotalIncomeByMonth(5, 2026, 4)).thenReturn(new BigDecimal("6000.00"));
        when(expenseClient.getTotalExpenseByMonth(5, 2026, 4)).thenReturn(new BigDecimal("3500.00"));
        when(expenseClient.getExpenseBreakdownByCategory(5, 2026, 4)).thenReturn(Map.of("Food", new BigDecimal("3500.00")));
        when(analyticsRepository.findByUserIdAndYearAndMonth(5, 2026, 4)).thenReturn(Optional.of(existing));
        when(analyticsRepository.save(any(FinancialSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialSnapshot snapshot = analyticsService.generateMonthlySnapshot(5, 2026, 4);

        assertEquals(99, snapshot.getSnapshotId());
        assertEquals("MONTHLY", snapshot.getPeriod());
        assertEquals(new BigDecimal("6000.00"), snapshot.getTotalIncome());
        assertEquals(new BigDecimal("3500.00"), snapshot.getTotalExpenses());
        assertEquals(new BigDecimal("2500.00"), snapshot.getNetSavings());
    }

    @Test
    void getFinancialHealthScore_CalculatesCorrectly() {
        // Arrange
        // Income = 5000, Expense = 2500 -> Savings = 2500 (50%). 
        // 50% savings is > 20% target, so Savings Score = 40/40.
        when(incomeClient.getTotalIncomeByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("5000.00"));
        when(expenseClient.getTotalExpenseByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("2500.00"));
        
        // Budget Adherence = 90%. 
        // Score = 40 - (0.9 * 40) = 40 - 36 = 4/40
        when(budgetClient.getOverallBudgetAdherence(anyInt())).thenReturn(90.0);
        
        // Expense Ratio = 2500/5000 = 0.5. 
        // Score = 20 - (0.5 * 20) = 20 - 10 = 10/20
        // Total Expected Score = 40 + 4 + 10 = 54

        // Act
        Integer score = analyticsService.getFinancialHealthScore(5);

        // Assert
        assertEquals(54, score);
    }

    @Test
    void getFinancialHealthScore_WithZeroIncome_StillReturnsValue() {
        when(incomeClient.getTotalIncomeByMonth(anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(expenseClient.getTotalExpenseByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("750.00"));
        when(budgetClient.getOverallBudgetAdherence(anyInt())).thenReturn(null);

        Integer score = analyticsService.getFinancialHealthScore(5);

        assertNotNull(score);
        assertTrue(score >= 0);
    }

    @Test
    void summaryAndTrendMethods_ReturnExpectedShapes() {
        when(incomeClient.getTotalIncomeByMonth(5, 2026, 4)).thenReturn(new BigDecimal("8000.00"));
        when(expenseClient.getTotalExpenseByMonth(5, 2026, 4)).thenReturn(new BigDecimal("3000.00"));
        when(expenseClient.getExpenseBreakdownByCategory(5, 2026, 4))
                .thenReturn(Map.of("Food", new BigDecimal("1200.00"), "Bills", new BigDecimal("800.00")));

        MonthlySummary monthlySummary = analyticsService.getMonthlySummary(5, 2026, 4);
        assertEquals(2026, monthlySummary.getYear());
        assertEquals(4, monthlySummary.getMonth());
        assertEquals("Food", monthlySummary.getTopCategory());

        FinancialSnapshot jan = FinancialSnapshot.builder().month(1).totalIncome(new BigDecimal("5000")).totalExpenses(new BigDecimal("2000")).savingsRate(new BigDecimal("60.00")).build();
        FinancialSnapshot feb = FinancialSnapshot.builder().month(2).totalIncome(new BigDecimal("5500")).totalExpenses(new BigDecimal("2500")).savingsRate(new BigDecimal("54.54")).build();
        when(analyticsRepository.findByUserIdAndYear(5, 2026)).thenReturn(List.of(jan, feb));
        when(incomeClient.getTotalIncomeByYear(5, 2026)).thenReturn(new BigDecimal("10500.00"));
        when(expenseClient.getTotalExpenseByYear(5, 2026)).thenReturn(new BigDecimal("4500.00"));

        YearlySummary yearlySummary = analyticsService.getYearlySummary(5, 2026);
        assertEquals(new BigDecimal("10500.00"), yearlySummary.getTotalIncome());
        assertEquals(new BigDecimal("4500.00"), yearlySummary.getTotalExpenses());

        List<Map<String, Object>> incomeVsExpense = analyticsService.getIncomeVsExpenseTrend(5, 2026);
        List<Map<String, Object>> savingsTrend = analyticsService.getSavingsRateTrend(5, 2026);
        assertEquals(2, incomeVsExpense.size());
        assertEquals(2, savingsTrend.size());
    }

    @Test
    void getYearlySummary_WithNoSnapshots_UsesZeroAverage() {
        when(incomeClient.getTotalIncomeByYear(5, 2026)).thenReturn(new BigDecimal("10500.00"));
        when(expenseClient.getTotalExpenseByYear(5, 2026)).thenReturn(new BigDecimal("4500.00"));
        when(analyticsRepository.findByUserIdAndYear(5, 2026)).thenReturn(List.of());

        YearlySummary yearlySummary = analyticsService.getYearlySummary(5, 2026);

        assertEquals(new BigDecimal("6000.00"), yearlySummary.getNetSavings());
        assertEquals(BigDecimal.ZERO, yearlySummary.getAverageSavingsRate());
    }

    @Test
    void getSnapshotById_ThrowsWhenMissing() {
        when(analyticsRepository.findById(404)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> analyticsService.getSnapshotById(404));

        assertTrue(exception.getMessage().contains("Snapshot not found"));
    }

    @Test
    void categoryDailyCashflowAndSnapshotHelpers_WorkAsExpected() {
        when(expenseClient.getExpenseBreakdownByCategory(5, 2026, 4))
                .thenReturn(Map.of("Food", new BigDecimal("400.00"), "Travel", new BigDecimal("900.00")));

        List<Map.Entry<String, BigDecimal>> top = analyticsService.getTopSpendingCategories(5, 4);
        assertFalse(top.isEmpty());
        assertEquals("Travel", top.get(0).getKey());

        when(expenseClient.getDailyExpenseTrend(5, 2026, 4))
                .thenReturn(Map.of(2, new BigDecimal("120.00"), 5, new BigDecimal("300.00")));
        assertEquals(2, analyticsService.getDailyExpenseTrend(5, 2026, 4).size());

        when(incomeClient.getTotalIncomeByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("7000.00"));
        when(expenseClient.getTotalExpenseByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("2800.00"));

        Map<String, BigDecimal> cashflow = analyticsService.getCashflowData(5, 4);
        assertEquals(new BigDecimal("7000.00"), cashflow.get("Inflow"));
        assertEquals(new BigDecimal("2800.00"), cashflow.get("Outflow"));

        SnapshotDto snapshotDto = analyticsService.getMonthlySnapshotDto(5, 4, 2026);
        assertEquals(new BigDecimal("4200.00"), snapshotDto.getNetSavings());
    }

    @Test
    void helperMethods_HandleEmptyAndNullClientData() {
        when(expenseClient.getExpenseBreakdownByCategory(5, 2026, 4)).thenReturn(null);
        assertTrue(analyticsService.getTopSpendingCategories(5, 4).isEmpty());

        when(expenseClient.getDailyExpenseTrend(5, 2026, 4)).thenReturn(null);
        assertTrue(analyticsService.getDailyExpenseTrend(5, 2026, 4).isEmpty());

        when(incomeClient.getTotalIncomeByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("1000.00"));
        when(expenseClient.getTotalExpenseByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("100.00"));

        Map<String, BigDecimal> cashflow = analyticsService.getCashflowData(5, 4);
        assertEquals(new BigDecimal("1000.00"), cashflow.get("Inflow"));
        assertEquals(new BigDecimal("100.00"), cashflow.get("Outflow"));
    }

    @Test
    void webChartMethods_CalculateFromClientData() {
        ExpenseDto expenseOne = new ExpenseDto();
        expenseOne.setCategoryId(10);
        expenseOne.setAmount(new BigDecimal("100.00"));
        expenseOne.setDate(LocalDate.of(2026, 4, 2));

        ExpenseDto expenseTwo = new ExpenseDto();
        expenseTwo.setCategoryId(10);
        expenseTwo.setAmount(new BigDecimal("50.00"));
        expenseTwo.setDate(LocalDate.of(2026, 4, 10));

        ExpenseDto expenseThree = new ExpenseDto();
        expenseThree.setCategoryId(20);
        expenseThree.setAmount(new BigDecimal("25.00"));
        expenseThree.setDate(LocalDate.of(2026, 3, 10));

        when(expenseClient.getUserExpenses(5)).thenReturn(List.of(expenseOne, expenseTwo, expenseThree));

        Map<String, Double> categoryChart = analyticsService.calculateCategorySpending(5, 4, 2026);
        assertEquals(150.0, categoryChart.get("10"));
        assertNull(categoryChart.get("20"));

        when(incomeClient.getTotalIncomeByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("1000.00"));
        when(expenseClient.getTotalExpenseByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("600.00"));

        Map<String, Object> sixMonthCashflow = analyticsService.calculate6MonthCashflow(5, 4, 2026);
        assertEquals(6, ((List<?>) sixMonthCashflow.get("months")).size());
        assertEquals(6, ((List<?>) sixMonthCashflow.get("incomes")).size());
        assertEquals(6, ((List<?>) sixMonthCashflow.get("expenses")).size());

        when(budgetClient.getOverallBudgetAdherence(5)).thenReturn(80.0);
        Integer healthScore = analyticsService.calculateHealthScore(5, 4);
        assertNotNull(healthScore);
        assertTrue(healthScore >= 0);
    }

    @Test
    void calculateCategorySpending_FiltersByMonthAndYear() {
        ExpenseDto januaryExpense = new ExpenseDto();
        januaryExpense.setCategoryId(10);
        januaryExpense.setAmount(new BigDecimal("30.00"));
        januaryExpense.setDate(LocalDate.of(2026, 1, 10));

        ExpenseDto aprilExpense = new ExpenseDto();
        aprilExpense.setCategoryId(10);
        aprilExpense.setAmount(new BigDecimal("70.00"));
        aprilExpense.setDate(LocalDate.of(2026, 4, 10));

        when(expenseClient.getUserExpenses(5)).thenReturn(List.of(januaryExpense, aprilExpense));

        Map<String, Double> result = analyticsService.calculateCategorySpending(5, 4, 2026);

        assertEquals(70.0, result.get("10"));
        assertFalse(result.containsKey("20"));
    }

    @Test
    void calculate6MonthCashflow_ProducesSixPeriods() {
        when(incomeClient.getTotalIncomeByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("2000.00"));
        when(expenseClient.getTotalExpenseByMonth(anyInt(), anyInt(), anyInt())).thenReturn(new BigDecimal("1000.00"));

        Map<String, Object> result = analyticsService.calculate6MonthCashflow(5, 4, 2026);

        assertEquals(6, ((List<?>) result.get("months")).size());
        assertEquals(6, ((List<?>) result.get("incomes")).size());
        assertEquals(6, ((List<?>) result.get("expenses")).size());
    }
}