package com.spendsmart.analytics;

import com.spendsmart.analytics.client.BudgetClient;
import com.spendsmart.analytics.client.ExpenseClient;
import com.spendsmart.analytics.client.IncomeClient;
import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.repository.AnalyticsRepository;
import com.spendsmart.analytics.service.AnalyticsServiceImpl; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
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
}