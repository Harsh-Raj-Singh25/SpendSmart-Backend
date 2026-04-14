package com.spendsmart.analytics.resource;

import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.model.dto.MonthlySummary;
import com.spendsmart.analytics.model.dto.YearlySummary;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsResource {

	private final AnalyticsService analyticsService;

	@PostMapping("/user/{userId}/snapshot")
	public ResponseEntity<FinancialSnapshot> generateSnapshot(@PathVariable Integer userId, @RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(analyticsService.generateMonthlySnapshot(userId, year, month));
	}

	@GetMapping("/user/{userId}/health")
	public ResponseEntity<Integer> getHealthScore(@PathVariable Integer userId) {
		return ResponseEntity.ok(analyticsService.getFinancialHealthScore(userId));
	}

	@GetMapping("/user/{userId}/forecast")
	public ResponseEntity<BigDecimal> getForecast(@PathVariable Integer userId) {
		return ResponseEntity.ok(analyticsService.getSpendingForecast(userId));
	}

	@GetMapping("/user/{userId}/summary/monthly")
	public ResponseEntity<MonthlySummary> getMonthlySummary(@PathVariable Integer userId, @RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(analyticsService.getMonthlySummary(userId, year, month));
	}

	@GetMapping("/user/{userId}/summary/yearly")
	public ResponseEntity<YearlySummary> getYearlySummary(@PathVariable Integer userId, @RequestParam int year) {
		return ResponseEntity.ok(analyticsService.getYearlySummary(userId, year));
	}

	@GetMapping("/user/{userId}/breakdown/category")
	public ResponseEntity<Map<String, BigDecimal>> getCategoryBreakdown(@PathVariable Integer userId,
			@RequestParam int year, @RequestParam int month) {
		return ResponseEntity.ok(analyticsService.getExpenseBreakdownByCategory(userId, year, month));
	}

	@GetMapping("/user/{userId}/trend/income-expense")
	public ResponseEntity<List<Map<String, Object>>> getIncomeVsExpenseTrend(@PathVariable Integer userId,
			@RequestParam int year) {
		return ResponseEntity.ok(analyticsService.getIncomeVsExpenseTrend(userId, year));
	}

	@GetMapping("/user/{userId}/trend/savings")
	public ResponseEntity<List<Map<String, Object>>> getSavingsRateTrend(@PathVariable Integer userId,
			@RequestParam int year) {
		return ResponseEntity.ok(analyticsService.getSavingsRateTrend(userId, year));
	}

	@GetMapping("/user/{userId}/top-categories")
	public ResponseEntity<List<Map.Entry<String, BigDecimal>>> getTopCategories(@PathVariable Integer userId,
			@RequestParam int month) {
		return ResponseEntity.ok(analyticsService.getTopSpendingCategories(userId, month));
	}

	@GetMapping("/user/{userId}/trend/daily")
	public ResponseEntity<List<Map<String, Object>>> getDailyTrend(@PathVariable Integer userId, @RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(analyticsService.getDailyExpenseTrend(userId, year, month));
	}

	@GetMapping("/user/{userId}/cashflow")
	public ResponseEntity<Map<String, BigDecimal>> getCashflow(@PathVariable Integer userId, @RequestParam int month) {
		return ResponseEntity.ok(analyticsService.getCashflowData(userId, month));
	}
}