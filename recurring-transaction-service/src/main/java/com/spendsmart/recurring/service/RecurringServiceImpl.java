package com.spendsmart.recurring.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spendsmart.recurring.client.ExpenseClient;
import com.spendsmart.recurring.client.IncomeClient;
import com.spendsmart.recurring.client.NotificationClient;
import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.model.dto.NotificationRequest;
import com.spendsmart.recurring.model.dto.TransactionRequest;
import com.spendsmart.recurring.model.enums.Frequency;
import com.spendsmart.recurring.repository.RecurringRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringServiceImpl implements RecurringService {
	private final RecurringRepository recurringRepository;
	private final ExpenseClient expenseClient;
	private final IncomeClient incomeClient;
	private final NotificationClient notificationClient;

	@Override
	public RecurringTransaction addRecurring(RecurringTransaction transaction) {
		return recurringRepository.save(transaction);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RecurringTransaction> getAllRecurring() {
		return recurringRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<RecurringTransaction> getByUser(Integer userId) {
		return recurringRepository.findByUserId(userId);
	}

	@Override
	public Optional<RecurringTransaction> getById(Integer recurringId) {
		// TODO Auto-generated method stub
		return recurringRepository.findById(recurringId);
	}

	@Override
	public List<RecurringTransaction> getActiveRecurring(Integer userId) {
		// TODO Auto-generated method stub
		return recurringRepository.findByUserIdAndIsActive(userId, true);
	}

	@Override
	public void deleteRecurring(Integer recurringId) {
		// TODO Auto-generated method stub
		recurringRepository.deleteById(recurringId);
	}

	@Override
	public RecurringTransaction updateRecurring(Integer recurringId, RecurringTransaction details) {
		// TODO Auto-generated method stub
		log.info("updating recurring of id :{}", recurringId);
		RecurringTransaction existing = getById(recurringId).orElseThrow(() -> new RuntimeException("Not found"));
		existing.setTitle(details.getTitle());
		existing.setAmount(details.getAmount());
		existing.setFrequency(details.getFrequency());
		existing.setEndDate(details.getEndDate());

		return recurringRepository.save(existing);
	}

	@Override
	public void deactivateRecurring(Integer recurringId) {
		// TODO Auto-generated method stub
		log.info("Deactivating recurring transaction of :{}", recurringId);
		RecurringTransaction existing = getById(recurringId).orElseThrow(() -> new RuntimeException("Not found"));
		existing.setIsActive(false);
		recurringRepository.save(existing);
	}

	// CORE AUTOMATION LOGIC

//	This method runs automatically every day at 00:00 (Midnight).
//   cron = "Seconds Minutes Hours Day-of-month Month Day-of-week"
	@Override
	@Scheduled(cron = "0 0 0 * * *")
//	@Scheduled(fixedRate = 10000)       // Run every 10 seconds for testing
	public void processUpcomingDue() {
		// TODO Auto-generated method stub
		LocalDate today = LocalDate.now();
		log.info("Running daily scheduled task to process recurring transactions for: {}", today);

		// 1. Find all active transactions where the due date is today (or missed dates
		// in the past)
		List<RecurringTransaction> dueTransactions = recurringRepository
				.findByIsActiveTrueAndNextDueDateLessThanEqual(today);

		for (RecurringTransaction recurring : dueTransactions) {
			try {
				// generate actual expense/income record...
				generateTransactionFromRecurring(recurring);
				// calculate the next due date
				LocalDate newDueDate = calculateNextDueDate(recurring.getNextDueDate(), recurring.getFrequency());

				// Check if the new date is past the user's defined End Date
				if (recurring.getEndDate() != null && newDueDate.isAfter(recurring.getEndDate())) {
					recurring.setIsActive(false);
					log.info("Recurring transaction {} has reached its end date. Deactivating.",
							recurring.getRecurringId());
				} else
					recurring.setNextDueDate(newDueDate);

				recurringRepository.save(recurring);
				log.info("Successfully processed recurring transaction {}. Next due date: {}",
						recurring.getRecurringId(), recurring.getNextDueDate());
			} catch (Exception e) {
				log.error("Failed to process recurring transaction {}: {}", recurring.getRecurringId(), e.getMessage());
			}
		}

		// for sending 3 day prior reminder...
		// NEW: Find transactions due exactly in 3 days for the Reminder
		LocalDate threeDaysFromNow = today.plusDays(3);
		List<RecurringTransaction> reminders = recurringRepository
				.findByIsActiveTrueAndNextDueDateLessThanEqual(threeDaysFromNow);

		for (RecurringTransaction r : reminders) {
			if (r.getNextDueDate().equals(threeDaysFromNow)) { // Only send on exactly day 3
				sendReminder(r);
			}
		}

	}

	@Override
	public LocalDate calculateNextDueDate(LocalDate currentDate, Frequency frequency) {
		// TODO Auto-generated method stub
		return switch (frequency) {
		case DAILY -> currentDate.plusDays(1);
		case WEEKLY -> currentDate.plusWeeks(1);
		case MONTHLY -> currentDate.plusMonths(1);
		case QUARTERLY -> currentDate.plusMonths(3);
		case YEARLY -> currentDate.plusYears(1);
		};
	}

	@Override
	public void generateTransactionFromRecurring(RecurringTransaction recurring) {
		// TODO Auto-generated method stub
		TransactionRequest request = TransactionRequest.builder().userId(recurring.getUserId())
				.categoryId(recurring.getCategoryId()).title(recurring.getTitle()).amount(recurring.getAmount())
				.currency("INR").type(recurring.getType().name()).paymentMethod(recurring.getPaymentMethod())
				.date(LocalDate.now()).isRecurring(true).notes(recurring.getDescription()).build();

		// Route to the correct service via Feign
		switch (recurring.getType()) {
		case EXPENSE:
			expenseClient.addExpense(request);
			break;
		case INCOME:
			incomeClient.addIncome(request);
			break;

		}

	}

	@Override
	public List<RecurringTransaction> getUpcomingThisMonth(Integer userId) {
		// TODO Auto-generated method stub
		LocalDate today = LocalDate.now();
		LocalDate endOfMonth = YearMonth.now().atEndOfMonth();
		return recurringRepository.findByUserIdAndIsActiveTrueAndNextDueDateBetween(userId, today, endOfMonth);
	}

	// for 3 day reminder
	private void sendReminder(RecurringTransaction r) {
		try {
			// Build the exact payload the Notification Service is expecting
			NotificationRequest request = NotificationRequest.builder().userId(r.getUserId())
					.title("Upcoming Payment: " + r.getTitle())
					.message("Reminder: Your automated transaction of " + r.getAmount() + " for " + r.getTitle()
							+ " is due in exactly 3 days.")
					.type("BOTH") // Triggers both Database Save and SMTP Email
					.build();

			// Send it over the wire!
			notificationClient.sendNotification(request);

			log.info("Successfully sent 3-day reminder for transaction {}", r.getRecurringId());

		} catch (Exception e) {
			log.error("Failed to send 3-day reminder for transaction {}: {}", r.getRecurringId(), e.getMessage());
		}
	}

}
