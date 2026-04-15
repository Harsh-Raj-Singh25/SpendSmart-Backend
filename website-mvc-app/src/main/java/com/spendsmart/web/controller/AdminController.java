package com.spendsmart.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.spendsmart.web.clients.AnalyticsClient;
import com.spendsmart.web.clients.AuthClient;
import com.spendsmart.web.clients.ExpenseClient;
import com.spendsmart.web.clients.NotificationClient;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AuthClient authClient;
	private final ExpenseClient expenseClient;
	private final AnalyticsClient analyticsClient;
	private final NotificationClient notificationClient;

	@GetMapping("/dashboard")
	public ModelAndView adminDashboard() {
		return new ModelAndView("admin/dashboard");
	}

	@GetMapping("/users")
	public ModelAndView manageAllUsers() {
		return new ModelAndView("admin/users");
	}

	@PostMapping("/users/{id}/suspend")
	public String suspendUser(@PathVariable int id) {
		return "redirect:/admin/users";
	}

	@PostMapping("/users/{id}/delete")
	public String deleteUser(@PathVariable int id) {
		return "redirect:/admin/users";
	}

	@GetMapping("/expenses/all")
	public ModelAndView viewAllExpenses() {
		return new ModelAndView("admin/all-expenses");
	}

	@GetMapping("/incomes/all")
	public ModelAndView viewAllIncomes() {
		return new ModelAndView("admin/all-incomes");
	}

	@GetMapping("/analytics")
	public ModelAndView viewPlatformAnalytics() {
		return new ModelAndView("admin/platform-analytics");
	}

	@GetMapping("/analytics/total-spending")
	public ModelAndView viewTotalSpending() {
		return new ModelAndView("admin/total-spending");
	}

	@GetMapping("/analytics/top-users")
	public ModelAndView viewTopSpendingUsers() {
		return new ModelAndView("admin/top-users");
	}

	@GetMapping("/analytics/category-usage")
	public ModelAndView viewCategoryUsage() {
		return new ModelAndView("admin/category-usage");
	}

	@PostMapping("/notifications/send")
	public String sendPlatformNotification(@RequestParam String title, @RequestParam String message) {
		// Uses NotifService (Feign Client) to send Bulk alerts
		return "redirect:/admin/dashboard?alertSent=true";
	}

	@GetMapping("/alerts")
	public ModelAndView viewBudgetAlerts() {
		return new ModelAndView("admin/system-alerts");
	}

	@GetMapping("/reports/generate")
	public ModelAndView generatePlatformReport() {
		return new ModelAndView("admin/report-viewer");
	}

	@GetMapping("/audit-logs")
	public ModelAndView viewAuditLogs() {
		return new ModelAndView("admin/audit-logs");
	}
}