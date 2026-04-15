package com.spendsmart.web.controller;  

import com.spendsmart.web.clients.AnalyticsClient;
import com.spendsmart.web.clients.BudgetClient;
import com.spendsmart.web.clients.ExpenseClient;
import com.spendsmart.web.clients.IncomeClient;
import com.spendsmart.web.clients.NotificationClient;
import com.spendsmart.web.clients.RecurringClient;
import com.spendsmart.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/app/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

	private final AnalyticsClient analyticsClient;
	private final ExpenseClient expenseClient;
	private final IncomeClient incomeClient;
	private final BudgetClient budgetClient;
	private final RecurringClient recurringClient;
	private final NotificationClient notificationClient;

	// Helper to mock the logged-in user until Spring Security is active
	private int getCurrentUserId() {
		return 5;
	}

	// ==========================================
	// --- HTML VIEW ENDPOINTS ---
	// ==========================================

	@GetMapping("/charts")
	public ModelAndView viewCharts() {
		// Just returns the empty canvas page; JavaScript will fetch the data via AJAX
		// below
		return new ModelAndView("charts-dashboard");
	}

	@GetMapping("/budgets")
	public ModelAndView viewBudgets() {
		ModelAndView mav = new ModelAndView("budgets");
		List<BudgetDto> userBudgets = budgetClient.getUserBudgets(getCurrentUserId());
		mav.addObject("budgets", userBudgets);
		mav.addObject("newBudget", new BudgetDto());
		return mav;
	}

	@PostMapping("/budgets/add")
	public String addBudget(@ModelAttribute BudgetDto budget) {
		budget.setUserId(getCurrentUserId());
		budgetClient.addBudget(budget);
		return "redirect:/app/analytics/budgets";
	}

	@GetMapping("/recurring")
	public ModelAndView viewRecurring() {
		ModelAndView mav = new ModelAndView("recurring-transactions");
		List<RecurringDto> activeRecurring = recurringClient.getActiveRecurringTransactions(getCurrentUserId());
		mav.addObject("recurringRules", activeRecurring);
		mav.addObject("newRecurring", new RecurringDto());
		return mav;
	}

	@PostMapping("/recurring/add")
	public String addRecurring(@ModelAttribute RecurringDto recurring) {
		recurring.setUserId(getCurrentUserId());
		recurringClient.addRecurringTransaction(recurring);
		return "redirect:/app/analytics/recurring";
	}

	// ==========================================
	// --- AJAX DATA ENDPOINTS (For Chart.js) ---
	// ==========================================

	@GetMapping("/api/monthly-summary")
	public ResponseEntity<SnapshotDto> getMonthlySummary(@RequestParam int month, @RequestParam int year) {
		return ResponseEntity.ok(analyticsClient.getMonthlySnapshot(getCurrentUserId(), month, year));
	}

	/*
	 * Note: The following methods assume your AnalyticsClient (and Analytics
	 * Microservice) has these specific endpoints exposed to calculate chart data.
	 */

	@GetMapping("/api/category-pie")
	public ResponseEntity<Map<String, Double>> getCategoryPieChart(@RequestParam int month, @RequestParam int year) {
		// Fetches a map of "Category Name" -> "Total Amount"
		return ResponseEntity.ok(analyticsClient.getCategoryPieChart(getCurrentUserId(), month, year));
	}

	@GetMapping("/api/cashflow")
	public ResponseEntity<Map<String, Object>> getCashflowData(@RequestParam int month, @RequestParam int year) {
		// Expected to return { "months": [...], "incomes": [...], "expenses": [...] }
		return ResponseEntity.ok(analyticsClient.getCashflowData(getCurrentUserId(), month, year));
	}

	@GetMapping("/api/health-score")
	public ResponseEntity<Integer> getHealthScore(@RequestParam int month) {
		return ResponseEntity.ok(analyticsClient.getFinancialHealthScore(getCurrentUserId(), month));
	}
}