package com.spendsmart.recurring.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.model.enums.Frequency;

public interface RecurringService {
	RecurringTransaction addRecurring(RecurringTransaction transaction);

	List<RecurringTransaction> getByUser(Integer userId);

	Optional<RecurringTransaction> getById(Integer recurringId);

	List<RecurringTransaction> getActiveRecurring(Integer userId);

	RecurringTransaction updateRecurring(Integer recurringId, RecurringTransaction transactionDetails);

	void deactivateRecurring(Integer recurringId);

	void deleteRecurring(Integer recurringId);

	// Automation Methods
	void processUpcomingDue();

	LocalDate calculateNextDueDate(LocalDate currentDate, Frequency frequency);

	void generateTransactionFromRecurring(RecurringTransaction recurring);

	List<RecurringTransaction> getUpcomingThisMonth(Integer userId);
}
