package com.spendsmart.recurring.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.model.enums.Frequency;
import com.spendsmart.recurring.model.enums.TransactionType;

@Repository
public interface RecurringRepository extends JpaRepository<RecurringTransaction, Integer> {

	List<RecurringTransaction> findByUserId(Integer userId);

	List<RecurringTransaction> findByUserIdAndType(Integer userId, TransactionType type);

	List<RecurringTransaction> findByUserIdAndIsActive(Integer userId, Boolean isActive);

	// CRITICAL for the Scheduled Job: Find all active transactions due today or
	// earlier
	List<RecurringTransaction> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);

	// Find upcoming for a specific user this month
	List<RecurringTransaction> findByUserIdAndIsActiveTrueAndNextDueDateBetween(Integer userId, LocalDate start,
			LocalDate end);

	List<RecurringTransaction> findByFrequency(Frequency frequency);

	int countByUserIdAndIsActive(Integer userId, Boolean isActive);

	Optional<RecurringTransaction> findByRecurringId(Integer recurringId);
}
