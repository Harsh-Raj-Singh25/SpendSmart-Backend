package com.spendsmart.recurring;

import com.spendsmart.recurring.client.ExpenseClient;
import com.spendsmart.recurring.client.IncomeClient;
import com.spendsmart.recurring.client.NotificationClient;
import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.model.dto.TransactionRequest;
import com.spendsmart.recurring.model.enums.Frequency;
import com.spendsmart.recurring.model.enums.TransactionType;
import com.spendsmart.recurring.repository.RecurringRepository;
import com.spendsmart.recurring.service.RecurringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringServiceImplTest {

	@Mock
	private RecurringRepository recurringRepository;

	@Mock
	private ExpenseClient expenseClient;

	@Mock
	private IncomeClient incomeClient;

	@Mock
	private NotificationClient notificationClient;

	@InjectMocks
	private RecurringServiceImpl recurringService;

	private RecurringTransaction mockTransaction;

	@BeforeEach
	void setUp() {
		mockTransaction = RecurringTransaction.builder().recurringId(1).userId(5).categoryId(10).title("Internet Bill")
				.amount(new BigDecimal("1000.00")).type(TransactionType.EXPENSE).frequency(Frequency.MONTHLY)
				.startDate(LocalDate.now()).nextDueDate(LocalDate.now()).isActive(true).paymentMethod("CARD").build();
	}

	@Test
	void calculateNextDueDate_Monthly_AddsOneMonth() {
		LocalDate current = LocalDate.of(2026, 4, 15);
		LocalDate next = recurringService.calculateNextDueDate(current, Frequency.MONTHLY);
		assertEquals(LocalDate.of(2026, 5, 15), next);
	}

	@Test
	void calculateNextDueDate_Yearly_AddsOneYear() {
		LocalDate current = LocalDate.of(2026, 4, 15);
		LocalDate next = recurringService.calculateNextDueDate(current, Frequency.YEARLY);
		assertEquals(LocalDate.of(2027, 4, 15), next);
	}

	@Test
	void processUpcomingDue_TriggersExpenseClientAndUpdatesDate() {
		// Arrange: Repository returns our mock transaction that is due today
		when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
				.thenReturn(List.of(mockTransaction));

		// Act: Run the scheduled job
		recurringService.processUpcomingDue();

		// Assert: Verify Feign client was called to create the expense
		verify(expenseClient, times(1)).addExpense(any(TransactionRequest.class));
		verify(incomeClient, never()).addIncome(any()); // Should not trigger income

		// Assert: Verify the date was moved forward by 1 month and saved
		assertEquals(LocalDate.now().plusMonths(1), mockTransaction.getNextDueDate());
		verify(recurringRepository, times(1)).save(mockTransaction);
	}

	@Test
	void basicCrudMethods_DelegateToRepository() {
		when(recurringRepository.save(mockTransaction)).thenReturn(mockTransaction);
		assertEquals(1, recurringService.addRecurring(mockTransaction).getRecurringId());

		when(recurringRepository.findByUserId(5)).thenReturn(List.of(mockTransaction));
		assertEquals(1, recurringService.getByUser(5).size());

		when(recurringRepository.findById(1)).thenReturn(Optional.of(mockTransaction));
		assertTrue(recurringService.getById(1).isPresent());

		when(recurringRepository.findByUserIdAndIsActive(5, true)).thenReturn(List.of(mockTransaction));
		assertEquals(1, recurringService.getActiveRecurring(5).size());

		recurringService.deleteRecurring(1);
		verify(recurringRepository).deleteById(1);
	}

	@Test
	void updateDeactivateAndUpcomingBehaviors_WorkAsExpected() {
		RecurringTransaction details = RecurringTransaction.builder()
				.title("Updated")
				.amount(new BigDecimal("1200.00"))
				.frequency(Frequency.WEEKLY)
				.endDate(LocalDate.now().plusMonths(1))
				.build();

		when(recurringRepository.findById(1)).thenReturn(Optional.of(mockTransaction));
		when(recurringRepository.save(any(RecurringTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

		RecurringTransaction updated = recurringService.updateRecurring(1, details);
		assertEquals("Updated", updated.getTitle());
		assertEquals(Frequency.WEEKLY, updated.getFrequency());

		recurringService.deactivateRecurring(1);
		assertFalse(mockTransaction.getIsActive());

		when(recurringRepository.findByUserIdAndIsActiveTrueAndNextDueDateBetween(eq(5), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(List.of(mockTransaction));
		assertEquals(1, recurringService.getUpcomingThisMonth(5).size());
	}

	@Test
	void generateTransactionFromRecurring_RoutesToIncomeWhenTypeIsIncome() {
		RecurringTransaction incomeRecurring = RecurringTransaction.builder()
				.recurringId(2)
				.userId(5)
				.categoryId(11)
				.title("Freelance")
				.amount(new BigDecimal("5000.00"))
				.type(TransactionType.INCOME)
				.frequency(Frequency.MONTHLY)
				.startDate(LocalDate.now())
				.nextDueDate(LocalDate.now())
				.isActive(true)
				.paymentMethod("BANK")
				.build();

		recurringService.generateTransactionFromRecurring(incomeRecurring);

		verify(incomeClient, times(1)).addIncome(any(TransactionRequest.class));
		verify(expenseClient, never()).addExpense(any(TransactionRequest.class));
	}

	@Test
	void processUpcomingDue_SendsReminderForThreeDayDue() {
		RecurringTransaction reminderTxn = RecurringTransaction.builder()
				.recurringId(3)
				.userId(5)
				.categoryId(10)
				.title("EMI")
				.amount(new BigDecimal("2000.00"))
				.type(TransactionType.EXPENSE)
				.frequency(Frequency.MONTHLY)
				.startDate(LocalDate.now())
				.nextDueDate(LocalDate.now().plusDays(3))
				.isActive(true)
				.paymentMethod("UPI")
				.build();

		when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
				.thenReturn(List.of())
				.thenReturn(List.of(reminderTxn));

		recurringService.processUpcomingDue();

		verify(notificationClient, atLeastOnce()).sendNotification(any());
	}
}