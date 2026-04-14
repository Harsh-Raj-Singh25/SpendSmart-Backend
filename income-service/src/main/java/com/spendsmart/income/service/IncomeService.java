package com.spendsmart.income.service;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.model.enums.IncomeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
	Income addIncome(Income income);

	Income getIncomeById(Integer incomeId);

	List<Income> getIncomesByUser(Integer userId);

	List<Income> getIncomesBySource(Integer userId, IncomeSource source);

	List<Income> getIncomesByDateRange(Integer userId, LocalDate startDate, LocalDate endDate);

	List<Income> getIncomesByMonth(Integer userId, int year, int month);

	Income updateIncome(Integer incomeId, Income incomeDetails);

	void deleteIncome(Integer incomeId);

	BigDecimal getTotalIncomeByUser(Integer userId);

	BigDecimal getTotalIncomeByMonth(Integer userId, int year, int month);  // also used by analytics service
 
	List<Income> getRecurringIncomes(Integer userId);
	
	// for analytics-service
	BigDecimal getTotalIncomeByYear(Integer userId, int year); 
}