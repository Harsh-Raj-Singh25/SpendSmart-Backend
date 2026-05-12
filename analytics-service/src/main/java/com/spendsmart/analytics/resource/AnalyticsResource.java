package com.spendsmart.analytics.resource;

import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.model.dto.MonthlySummary;
import com.spendsmart.analytics.model.dto.SnapshotDto;
import com.spendsmart.analytics.model.dto.YearlySummary;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsResource {

	private final AnalyticsService analyticsService;

	// ── Admin Read-Only Endpoints ──────────────────────────────────────
	@GetMapping("/admin/snapshots")
	public ResponseEntity<List<FinancialSnapshot>> getAllSnapshots() {
		return ResponseEntity.ok(analyticsService.getAllSnapshots());
	}

	@GetMapping("/admin/snapshots/{snapshotId}")
	public ResponseEntity<FinancialSnapshot> getSnapshotById(@PathVariable Integer snapshotId) {
		return ResponseEntity.ok(analyticsService.getSnapshotById(snapshotId));
	}

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

	// for web mvc layer
	@GetMapping("/snapshot/user/{userId}/month/{month}/year/{year}")
	public ResponseEntity<SnapshotDto> getMonthlySnapshot(@PathVariable int userId, @PathVariable int month,
			@PathVariable int year) {
		log.info("Generating Monthly Snapshot for User: {} | Date: {}/{}", userId, month, year);
		return (ResponseEntity<SnapshotDto>) ResponseEntity.ok(analyticsService.getMonthlySnapshotDto(userId, month, year));
	}

	@GetMapping("/charts/category-pie")
	public ResponseEntity<Map<String, Double>> getCategoryPieChart(@RequestParam int userId, @RequestParam int month,
			@RequestParam int year) {
		log.info("Generating Category Pie Chart Data for User: {}", userId);
		return ResponseEntity.ok(analyticsService.calculateCategorySpending(userId, month, year));
	}

	@GetMapping("/charts/cashflow")
	public ResponseEntity<Map<String, Object>> getCashflowData(@RequestParam int userId, @RequestParam int month,
			@RequestParam int year) {
		log.info("Generating 6-Month Cashflow Data for User: {}", userId);
		return ResponseEntity.ok(analyticsService.calculate6MonthCashflow(userId, month, year));
	}

	@GetMapping("/health-score")
	public ResponseEntity<Integer> getFinancialHealthScore(@RequestParam int userId, @RequestParam int month) {
		log.info("Calculating Financial Health Score for User: {}", userId);
		return ResponseEntity.ok(analyticsService.calculateHealthScore(userId, month));
	}
}