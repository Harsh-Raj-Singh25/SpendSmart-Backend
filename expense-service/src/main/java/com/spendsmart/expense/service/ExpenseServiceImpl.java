package com.spendsmart.expense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;
import com.spendsmart.expense.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpenseServiceImpl  implements ExpenseService{
	private final ExpenseRepository expenseRepository; //

	@Override
	public Expense addExpense(Expense expense) { 
		log.info("Adding new expense for user: {}",expense.getUserId());
		Expense savedExpense=expenseRepository.save(expense);
		// TODO: As per document  , trigger Budget-Service to increment spentAmount
        // This will be implemented later via Kafka or OpenFeign
		return savedExpense;
	}

	@Override
	@Transactional(readOnly = true)
	public Expense getExpenseById(Long expenseId) {
		// TODO Auto-generated method stub
		return expenseRepository.findByExpenseId(expenseId)
				.orElseThrow(()-> new RuntimeException("Expense not found with ID: "+expenseId));
		
	}

	@Override
	@Transactional(readOnly= true)
	public List<Expense> getExpensesByUser(Integer userId) {
		// TODO Auto-generated method stub
		return expenseRepository.findByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByCategory(Long categoryId) {
		// TODO Auto-generated method stub
		return expenseRepository.findByCategoryId(categoryId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByDateRange(Integer userId,LocalDate start, LocalDate end) {
		// TODO Auto-generated method stub
		 return expenseRepository.findByUserIdAndDateBetween(userId, start, end);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByMonth(Integer userId, int year, int month) {
		// TODO Auto-generated method stub
		YearMonth yearMonth=YearMonth.of(year, month);
		LocalDate startDate=yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();
		return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByType(Integer userId, ExpenseType type) {
		// TODO Auto-generated method stub
		return expenseRepository.findByUserIdAndType(userId, type);
	}

	@Override
	public List<Expense> searchExpenses(Integer userId, String keyword) {
		// TODO Auto-generated method stub
		return expenseRepository.searchExpensesByKeyword(userId, keyword);
	}

	@Override
	public Expense updateExpense(Long expenseId, Expense expenseDetails) {
		// TODO Auto-generated method stub
		log.info("Updating expense ID:{}", expenseId);
		Expense existingExpense = getExpenseById(expenseId);
		
		existingExpense.setCategoryId(expenseDetails.getCategoryId());
		existingExpense.setTitle(expenseDetails.getTitle());
        existingExpense.setAmount(expenseDetails.getAmount());
        existingExpense.setCurrency(expenseDetails.getCurrency());
        existingExpense.setType(expenseDetails.getType());
        existingExpense.setPaymentMethod(expenseDetails.getPaymentMethod());
        existingExpense.setDate(expenseDetails.getDate());
        existingExpense.setNotes(expenseDetails.getNotes());
        existingExpense.setReceiptUrl(expenseDetails.getReceiptUrl());
//        existingExpense.setRecurring(expenseDetails.isRecurring()); -> if the primitive boolean is used
        existingExpense.setIsRecurring(expenseDetails.getIsRecurring());
        
     // TODO: Adjust Budget-Service (calculate difference and update)
        return expenseRepository.save(existingExpense);
	}

	@Override
	public void deleteExpense(Long expenseId) {
		// TODO Auto-generated method stub
		log.info("Deleting expense ID: {}", expenseId);
		getExpenseById(expenseId);
		// TODO: Notify Budget-Service to decrement spentAmount
		expenseRepository.deleteByExpenseId(expenseId);
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalByUser(Integer userId) {
		// TODO Auto-generated method stub
		BigDecimal total = expenseRepository.sumAmountByUserId(userId);
		return total !=null ? total : BigDecimal.ZERO;
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalByCategory(Long categoryId) {
		// TODO Auto-generated method stub
		BigDecimal total = expenseRepository.sumAmountByCategoryId(categoryId);
		return total != null ? total : BigDecimal.ZERO;
	}
	 
	
	
	
}
