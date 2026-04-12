package com.spendsmart.expense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spendsmart.expense.client.BudgetServiceClient;
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
	private final BudgetServiceClient budgetServiceClient; // <--  Inject the Client
	@Override
	public Expense addExpense(Expense expense) { 
		log.info("Adding new expense for user: {}",expense.getUserId());
		Expense savedExpense=expenseRepository.save(expense);
		// TODO: As per document  , trigger Budget-Service to increment spentAmount
        // This will be implemented later via Kafka or OpenFeign
		// implementation
		// 2. Call the Budget Service synchronously!
        try {
            budgetServiceClient.updateSpentAmountByCategory(
                    savedExpense.getUserId(), 
                    savedExpense.getCategoryId(), 
                    savedExpense.getAmount()
            );
            log.info("Successfully updated budget for category: {}", savedExpense.getCategoryId());
        } catch (Exception e) {
            log.error("Failed to update budget. Budget Service might be down: {}", e.getMessage());
            // We catch the error so the Expense still saves even if the Budget service is temporarily offline
        }
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
		
		// Store the old values BEFORE we change them so we can do math later
        BigDecimal oldAmount = existingExpense.getAmount();
        Long oldCategoryId = existingExpense.getCategoryId();
        
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
        
        Expense savedExpense = expenseRepository.save(existingExpense);
     // TODO: Adjust Budget-Service (calculate difference and update)
     // Sync with Budget Service
        try {
            if (!oldCategoryId.equals(savedExpense.getCategoryId())) {
                // Scenario A: The user changed the category of the expense!
                // 1. Refund the old budget
                budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), oldCategoryId, oldAmount.negate());
                // 2. Charge the new budget
                budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), savedExpense.getCategoryId(), savedExpense.getAmount());
                log.info("Cross-category budget sync completed.");
            } else if (oldAmount.compareTo(savedExpense.getAmount()) != 0) {
                // Scenario B: The category is the same, but the amount changed
                BigDecimal difference = savedExpense.getAmount().subtract(oldAmount);
                budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), savedExpense.getCategoryId(), difference);
                log.info("Budget sync completed. Amount adjusted by: {}", difference);
            }
        } catch (Exception e) {
            log.error("Failed to sync budget during expense update. Budget service might be down: {}", e.getMessage());
        }

        return savedExpense;
	}

	@Override
    public void deleteExpense(Long expenseId) {
        log.info("Deleting expense ID: {}", expenseId);
        Expense existingExpense = getExpenseById(expenseId); 
        
        expenseRepository.deleteByExpenseId(expenseId);

        // Sync with Budget Service by sending a negative amount (Refund)
        try {
            budgetServiceClient.updateSpentAmountByCategory(
                    existingExpense.getUserId(), 
                    existingExpense.getCategoryId(), 
                    existingExpense.getAmount().negate() // .negate() turns 500 into -500
            );
            log.info("Successfully refunded budget for deleted expense.");
        } catch (Exception e) {
            log.error("Failed to refund budget. Budget service might be down: {}", e.getMessage());
        }
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
