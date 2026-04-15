package com.spendsmart.income;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.spendsmart.income.client.AuthClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.model.enums.IncomeSource;
import com.spendsmart.income.repository.IncomeRepository;
import com.spendsmart.income.service.IncomeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class IncomeServiceImplTest {
	
	@Mock
	private IncomeRepository incomeRepository;

	@Mock
	private AuthClient authClient;
	
	@InjectMocks
	private IncomeServiceImpl incomeService;
	
	private Income mockIncome;
	
	@BeforeEach
	void setUp() {
		mockIncome=Income.builder()
				.incomeId(1)
				.userId(1)
				.categoryId(201)
				.title("Capgemini Monthly Salary")
				.amount(new BigDecimal("40000"))
				.currency("INR")
				.source(IncomeSource.SALARY)
				.date(LocalDate.now())
				.isRecurring(true)
				.recurrencePeriod("MONTHLY")
				.build();
	}
	
	@Test
	void addIncome_Success() {
		// Arrange
		Map<String, Object> subscriptionStatus = new HashMap<>();
		subscriptionStatus.put("subscriptionType", "FREE");
		when(authClient.getSubscriptionStatus(1)).thenReturn(subscriptionStatus);

		// Mock daily count: User has 0 incomes today (limit is 7)
		when(incomeRepository.countByUserIdAndDate(eq(1), any(LocalDate.class))).thenReturn(0L);

		when(incomeRepository.save(any(Income.class))).thenReturn(mockIncome);

		//Act
		Income savedIncome=incomeService.addIncome(mockIncome);
		
		//Assertions
		assertNotNull(savedIncome);
		assertEquals(1, savedIncome.getIncomeId());
		assertEquals(new BigDecimal("40000"),savedIncome.getAmount());
		assertEquals(IncomeSource.SALARY, savedIncome.getSource());
		verify(incomeRepository, times(1)).save(any(Income.class));
	}

	@Test
	void addIncome_DailyLimitReached_ThrowsException() {
		// Arrange
		Map<String, Object> subscriptionStatus = new HashMap<>();
		subscriptionStatus.put("subscriptionType", "FREE");
		when(authClient.getSubscriptionStatus(1)).thenReturn(subscriptionStatus);

		// Mock daily count: User already has 7 incomes today
		when(incomeRepository.countByUserIdAndDate(eq(1), any(LocalDate.class))).thenReturn(7L);

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			incomeService.addIncome(mockIncome);
		});

		assertTrue(exception.getMessage().contains("Daily limit reached"));
		verify(incomeRepository, never()).save(any(Income.class));
	}

	@Test
	void addIncome_PremiumUser_SuccessRegardlessOfCount() {
		// Arrange
		Map<String, Object> subscriptionStatus = new HashMap<>();
		subscriptionStatus.put("subscriptionType", "PREMIUM");
		when(authClient.getSubscriptionStatus(1)).thenReturn(subscriptionStatus);

		when(incomeRepository.save(any(Income.class))).thenReturn(mockIncome);

		// Act
		Income savedIncome = incomeService.addIncome(mockIncome);

		// Assert
		assertNotNull(savedIncome);
		verify(incomeRepository, times(1)).save(any(Income.class));
		// Verify countByUserIdAndDate was NEVER called for PREMIUM users
		verify(incomeRepository, never()).countByUserIdAndDate(anyInt(), any(LocalDate.class));
	}
	
	@Test
    void getIncomeById_Success() {
        // Arrange
        when(incomeRepository.findByIncomeId(1)).thenReturn(Optional.of(mockIncome));
        // Act
        Income foundIncome = incomeService.getIncomeById(1);
        // Assertions
        assertNotNull(foundIncome);
        assertEquals("Capgemini Monthly Salary", foundIncome.getTitle());
    }

    @Test
    void getIncomeById_NotFound_ThrowsException() {
        // Arrange
        when(incomeRepository.findByIncomeId(99)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            incomeService.getIncomeById(99);
        });
        assertEquals("Income not found with ID: 99", exception.getMessage());
    }

    @Test
    void getTotalIncomeByMonth_WithData_ReturnsTotal() {
        // Arrange
        // We use any() for the dates because YearMonth calculates them dynamically in the service
        when(incomeRepository.sumAmountByUserIdAndDateBetween(eq(1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("85000.00"));
        // Act
        BigDecimal total = incomeService.getTotalIncomeByMonth(1, 2026, 4);
        // Assertions
        assertEquals(new BigDecimal("85000.00"), total);
    }

    @Test
    void getTotalIncomeByMonth_NoData_ReturnsZero() {
        // Arrange
        when(incomeRepository.sumAmountByUserIdAndDateBetween(eq(1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(null);
        // Act
        BigDecimal total = incomeService.getTotalIncomeByMonth(1, 2026, 4);
        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }
}

