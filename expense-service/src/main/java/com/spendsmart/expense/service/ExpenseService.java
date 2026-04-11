package com.spendsmart.expense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;

public interface ExpenseService {
	Expense addExpense(Expense expense);
	
	Expense getExpenseById(Long expenseId);
	
	List<Expense> getExpensesByUser(Integer userId);
	
	List<Expense> getExpensesByCategory(Long categoryId);
	
	List<Expense> getExpensesByDateRange(Integer userId,LocalDate start, LocalDate end);
	
	List<Expense> getExpensesByMonth(Integer userId, int year, int month);
	
    List<Expense> getExpensesByType(Integer userId, ExpenseType type);
    
    List<Expense> searchExpenses(Integer userId, String keyword);
    
    Expense updateExpense(Long expenseId, Expense expenseDetails);
    
    void deleteExpense(Long expenseId);
    
    BigDecimal getTotalByUser(Integer userId);
    
    BigDecimal getTotalByCategory(Long categoryId);
}
