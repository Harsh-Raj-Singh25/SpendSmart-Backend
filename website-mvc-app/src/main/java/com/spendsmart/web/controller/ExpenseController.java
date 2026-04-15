package com.spendsmart.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.spendsmart.web.clients.AnalyticsClient;
import com.spendsmart.web.clients.CategoryClient;
import com.spendsmart.web.clients.ExpenseClient;
import com.spendsmart.web.clients.IncomeClient;
import com.spendsmart.web.clients.NotificationClient;
import com.spendsmart.web.dto.CategoryDto;
import com.spendsmart.web.dto.SnapshotDto;
import com.spendsmart.web.dto.TransactionDto;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

	// 1. Inject the Feign Clients
	private final ExpenseClient expenseClient;
	private final IncomeClient incomeClient;
	private final AnalyticsClient analyticsClient;
	private final NotificationClient notificationClient;
	private final CategoryClient categoryClient;

	// A helper method to mock the logged-in user ID until Security is integrated
	private int getCurrentUserId() {
		return 5;
	}

	@GetMapping("/dashboard")
	public ModelAndView viewDashboard() {
		int userId = getCurrentUserId();
		LocalDate today = LocalDate.now();

		ModelAndView mav = new ModelAndView("dashboard");

		try {
			// 2. Fetch the data from the microservices via the API Gateway
			SnapshotDto snapshot = analyticsClient.getMonthlySnapshot(userId, today.getMonthValue(), today.getYear());
			List<TransactionDto> expenses = expenseClient.getUserExpenses(userId);
			Integer unreadNotifs = notificationClient.getUnreadCount(userId);

			// 3. Attach the data to the UI Model
			mav.addObject("totalIncome", snapshot.getTotalIncome());
			mav.addObject("totalExpense", snapshot.getTotalExpenses());
			mav.addObject("netSavings", snapshot.getNetSavings());

			// Limit to the 5 most recent transactions for the dashboard view
			mav.addObject("recentTransactions", expenses.stream().limit(5).toList());
			mav.addObject("unreadCount", unreadNotifs);

		} catch (Exception e) {
			log.error("Failed to load dashboard data: {}", e.getMessage());
			mav.addObject("error", "Unable to load financial data. Please try again later.");
		}

		// 4. Return the view
		return mav;
	}

	// Example of receiving form data from the UI and sending it to the backend
	@PostMapping("/expenses/add")
	public String addExpense(@ModelAttribute TransactionDto expenseForm) {
		expenseForm.setUserId(getCurrentUserId());
		expenseForm.setType("EXPENSE");

		// Push the new expense to the backend microservice
		expenseClient.addExpense(expenseForm);

		// Redirect back to the dashboard to see the updated charts
		return "redirect:/app/dashboard";
	}

	@GetMapping("/home")
	public String home() {
		return "index"; // Renders index.html
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new Object());
		return "register";
	}

	@PostMapping("/login")
	public String login(Model model, String username, String password, Principal principal) {
		// Authenticate via Gateway/Auth Service
		return "redirect:/app/dashboard";
	}

//	@GetMapping("/dashboard")
//	public ModelAndView viewDashboard() {
//		ModelAndView mav = new ModelAndView("dashboard");
//		// mav.addObject("recentExpenses", expenseService.getRecent(...));
//		return mav;
//	}

	@GetMapping("/profile")
	public ModelAndView viewProfile() {
		return new ModelAndView("profile");
	}

	@PostMapping("/profile/edit")
	public String editProfile(@ModelAttribute Object user) {
		return "redirect:/app/profile";
	}

//	@GetMapping("/expenses/add")
//	public ModelAndView addExpense(@ModelAttribute Object expense) {
//		return new ModelAndView("expense-form");
//	}

	@GetMapping("/expenses/edit/{id}")
	public ModelAndView editExpense(@PathVariable int id) {
		return new ModelAndView("expense-form");
	}

	@PostMapping("/expenses/update/{id}")
	public ModelAndView updateExpense(@PathVariable int id, @ModelAttribute Object expense) {
		return new ModelAndView("redirect:/app/expenses");
	}

	@DeleteMapping("/expenses/{id}")
	public ResponseEntity<Void> deleteExpense(@PathVariable int id) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/expenses")
	public ModelAndView viewExpenses() {
		return new ModelAndView("expense-list");
	}

	@PostMapping("/expenses/filter")
	public ModelAndView filterExpenses(@RequestParam Map<String, String> filters) {
		return new ModelAndView("expense-list");
	}

	@GetMapping("/incomes/add")
	public ModelAndView addIncome() {
		return new ModelAndView("income-form");
	}

	@GetMapping("/incomes/edit/{id}")
	public ModelAndView editIncome(@PathVariable int id) {
		return new ModelAndView("income-form");
	}

	@PostMapping("/incomes/update")
	public ModelAndView updateIncome() {
		return new ModelAndView("redirect:/app/incomes");
	}

	@DeleteMapping("/incomes/{id}")
	public ModelAndView deleteIncome(@PathVariable int id) {
		return new ModelAndView("redirect:/app/incomes");
	}

	@GetMapping("/incomes")
	public ModelAndView viewIncomes() {
		return new ModelAndView("income-list");
	}
 

	@GetMapping("/categories")
	public ModelAndView manageCategories() {
		ModelAndView mav = new ModelAndView("categories");

		// Fetch real categories via the API Gateway
		List<CategoryDto> categories = categoryClient.getAllCategories();

		mav.addObject("categories", categories);
		mav.addObject("newCategory", new CategoryDto()); // Empty object for the form

		return mav;
	}

	@PostMapping("/categories/add")
	public String addCategory(@ModelAttribute CategoryDto newCategory) {
		// Send the filled form to the backend
		categoryClient.addCategory(newCategory);

		// Refresh the page
		return "redirect:/app/categories";
	}

	@PostMapping("/budget/set")
	public ModelAndView setBudget() {
		return new ModelAndView("redirect:/app/dashboard");
	}

	@GetMapping("/notifications")
	public ModelAndView viewNotifications() {
		return new ModelAndView("notifications");
	}

	@GetMapping("/logout")
	public String logout() {
		return "redirect:/app/login";
	}
}