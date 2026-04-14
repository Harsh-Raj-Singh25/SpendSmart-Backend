package com.spendsmart.recurring;

import com.spendsmart.recurring.client.ExpenseClient;
import com.spendsmart.recurring.client.IncomeClient;
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
}