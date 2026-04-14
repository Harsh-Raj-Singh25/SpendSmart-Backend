package com.spendsmart.analytics.service;

import com.spendsmart.analytics.client.BudgetClient;
import com.spendsmart.analytics.client.ExpenseClient;
import com.spendsmart.analytics.client.IncomeClient;
import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.model.dto.MonthlySummary;
import com.spendsmart.analytics.model.dto.YearlySummary;
import com.spendsmart.analytics.repository.AnalyticsRepository; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final IncomeClient incomeClient;
    private final ExpenseClient expenseClient;
    private final BudgetClient budgetClient;

    // ---  Snapshot Generation (Existing) ---
    @Override
    public FinancialSnapshot generateMonthlySnapshot(Integer userId, int year, int month) {
        BigDecimal income = getOrDefault(incomeClient.getTotalIncomeByMonth(userId, year, month));
        BigDecimal expenses = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, year, month));
        
        BigDecimal netSavings = income.subtract(expenses);
        BigDecimal savingsRate = calculatePercentage(netSavings, income);

        Map<String, BigDecimal> breakdown = expenseClient.getExpenseBreakdownByCategory(userId, year, month);
        String topCategory = getTopCategoryFromMap(breakdown);

        FinancialSnapshot snapshot = analyticsRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElse(new FinancialSnapshot());
        
        snapshot.setUserId(userId);
        snapshot.setPeriod("MONTHLY");
        snapshot.setYear(year);
        snapshot.setMonth(month);
        snapshot.setTotalIncome(income);
        snapshot.setTotalExpenses(expenses);
        snapshot.setNetSavings(netSavings);
        snapshot.setSavingsRate(savingsRate);
        snapshot.setTopCategory(topCategory);

        return analyticsRepository.save(snapshot);
    }

    // ---  Summaries ---
    @Override
    public MonthlySummary getMonthlySummary(Integer userId, int year, int month) {
        BigDecimal income = getOrDefault(incomeClient.getTotalIncomeByMonth(userId, year, month));
        BigDecimal expenses = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, year, month));
        String topCategory = getTopCategoryFromMap(expenseClient.getExpenseBreakdownByCategory(userId, year, month));

        return MonthlySummary.builder()
                .year(year)
                .month(month)
                .totalIncome(income)
                .totalExpense(expenses)
                .netSavings(income.subtract(expenses))
                .topCategory(topCategory)
                .build();
    }

    @Override
    public YearlySummary getYearlySummary(Integer userId, int year) {
        BigDecimal income = getOrDefault(incomeClient.getTotalIncomeByYear(userId, year));
        BigDecimal expenses = getOrDefault(expenseClient.getTotalExpenseByYear(userId, year));
        
        // Calculate average savings rate from historical snapshots
        List<FinancialSnapshot> snapshots = analyticsRepository.findByUserIdAndYear(userId, year);
        BigDecimal avgSavingsRate = snapshots.isEmpty() ? BigDecimal.ZERO : 
                snapshots.stream()
                         .map(FinancialSnapshot::getSavingsRate)
                         .reduce(BigDecimal.ZERO, BigDecimal::add)
                         .divide(new BigDecimal(snapshots.size()), 2, RoundingMode.HALF_UP);

        return YearlySummary.builder()
                .year(year)
                .totalIncome(income)
                .totalExpenses(expenses)
                .netSavings(income.subtract(expenses))
                .averageSavingsRate(avgSavingsRate)
                .build();
    }

    // ---  Breakdowns & Trends ---
    @Override
    public Map<String, BigDecimal> getExpenseBreakdownByCategory(Integer userId, int year, int month) {
        return expenseClient.getExpenseBreakdownByCategory(userId, year, month);
    }

    @Override
    public List<Map<String, Object>> getIncomeVsExpenseTrend(Integer userId, int year) {
        List<FinancialSnapshot> snapshots = analyticsRepository.findByUserIdAndYear(userId, year);
        return snapshots.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", s.getMonth());
            map.put("income", s.getTotalIncome());
            map.put("expense", s.getTotalExpenses());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSavingsRateTrend(Integer userId, int year) {
        List<FinancialSnapshot> snapshots = analyticsRepository.findByUserIdAndYear(userId, year);
        return snapshots.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", s.getMonth());
            map.put("savingsRate", s.getSavingsRate());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map.Entry<String, BigDecimal>> getTopSpendingCategories(Integer userId, int month) {
        // Fetches current year's month breakdown, sorts descending, and returns top 5
        Map<String, BigDecimal> breakdown = expenseClient.getExpenseBreakdownByCategory(userId, LocalDate.now().getYear(), month);
        if (breakdown == null) return new ArrayList<>();
        
        return breakdown.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDailyExpenseTrend(Integer userId, int year, int month) {
        Map<Integer, BigDecimal> dailyData = expenseClient.getDailyExpenseTrend(userId, year, month);
        List<Map<String, Object>> trend = new ArrayList<>();
        
        if (dailyData != null) {
            dailyData.forEach((day, amount) -> {
                Map<String, Object> point = new HashMap<>();
                point.put("day", day);
                point.put("amount", amount);
                trend.add(point);
            });
        }
        return trend;
    }

    @Override
    public Map<String, BigDecimal> getCashflowData(Integer userId, int month) {
        int year = LocalDate.now().getYear();
        BigDecimal income = getOrDefault(incomeClient.getTotalIncomeByMonth(userId, year, month));
        BigDecimal expenses = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, year, month));
        
        Map<String, BigDecimal> cashflow = new HashMap<>();
        cashflow.put("Inflow", income);
        cashflow.put("Outflow", expenses);
        return cashflow;
    }

    // --- 4. Complex Algorithms (Health Score & Forecast) ---
    @Override
    public Integer getFinancialHealthScore(Integer userId) {
        LocalDate now = LocalDate.now();
        BigDecimal income = getOrDefault(incomeClient.getTotalIncomeByMonth(userId, now.getYear(), now.getMonthValue()));
        BigDecimal expenses = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, now.getYear(), now.getMonthValue()));
        BigDecimal safeIncome = income.compareTo(BigDecimal.ZERO) > 0 ? income : new BigDecimal("1");

        BigDecimal savingsRate = income.subtract(expenses).divide(safeIncome, 2, RoundingMode.HALF_UP);
        double savingsScore = Math.min((savingsRate.doubleValue() / 0.20) * 40, 40.0);

        Double adherence = budgetClient.getOverallBudgetAdherence(userId);
        double budgetScore = Math.max(40.0 - ((getOrDefault(adherence) / 100.0) * 40), 0);

        BigDecimal expenseRatio = expenses.divide(safeIncome, 2, RoundingMode.HALF_UP);
        double ratioScore = Math.max(20.0 - (expenseRatio.doubleValue() * 20.0), 0);

        return (int) Math.max(0, Math.round(savingsScore + budgetScore + ratioScore));
    }

    @Override
    public BigDecimal getSpendingForecast(Integer userId) {
        LocalDate now = LocalDate.now();
        BigDecimal m1 = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, now.minusMonths(1).getYear(), now.minusMonths(1).getMonthValue()));
        BigDecimal m2 = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, now.minusMonths(2).getYear(), now.minusMonths(2).getMonthValue()));
        BigDecimal m3 = getOrDefault(expenseClient.getTotalExpenseByMonth(userId, now.minusMonths(3).getYear(), now.minusMonths(3).getMonthValue()));

        BigDecimal average = m1.add(m2).add(m3).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        BigDecimal momentum = m1.subtract(m3).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);

        return average.add(momentum).max(BigDecimal.ZERO);
    }
 
    private BigDecimal getOrDefault(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
    
    private Double getOrDefault(Double value) {
        return value != null ? value : 0.0;
    }

    private BigDecimal calculatePercentage(BigDecimal part, BigDecimal whole) {
        if (whole.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return part.divide(whole, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private String getTopCategoryFromMap(Map<String, BigDecimal> breakdown) {
        if (breakdown == null || breakdown.isEmpty()) return "None";
        return breakdown.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }
}