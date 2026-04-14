package com.spendsmart.analytics.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.model.dto.MonthlySummary;
import com.spendsmart.analytics.model.dto.YearlySummary;

public interface AnalyticsService {

	FinancialSnapshot generateMonthlySnapshot(Integer userId, int year, int month);

	Integer getFinancialHealthScore(Integer userId);

	BigDecimal getSpendingForecast(Integer userId);

	MonthlySummary getMonthlySummary(Integer userId, int year, int month);

	YearlySummary getYearlySummary(Integer userId, int year);

	Map<String, BigDecimal> getExpenseBreakdownByCategory(Integer userId, int year, int month);

	List<Map<String, Object>> getIncomeVsExpenseTrend(Integer userId, int year);

	List<Map<String, Object>> getSavingsRateTrend(Integer userId, int year);

	List<Entry<String, BigDecimal>> getTopSpendingCategories(Integer userId, int month);

	List<Map<String, Object>> getDailyExpenseTrend(Integer userId, int year, int month);

	Map<String, BigDecimal> getCashflowData(Integer userId, int month);

}
