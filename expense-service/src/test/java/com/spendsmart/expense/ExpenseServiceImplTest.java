package com.spendsmart.expense;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;
import com.spendsmart.expense.model.enums.PaymentMethod;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.ExpenseServiceImpl;
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
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense mockExpense;

    @BeforeEach
    void setUp() {
        mockExpense = Expense.builder()
                .expenseId(1L)
                .userId(1)
                .categoryId(101L)
                .title("Groceries")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .type(ExpenseType.EXPENSE)
                .paymentMethod(PaymentMethod.UPI)
                .date(LocalDate.now())
                .isRecurring(false)
                .build();
    }

    @Test
    void addExpense_Success() {
        // Arrange
        when(expenseRepository.save(any(Expense.class))).thenReturn(mockExpense);

        // Act
        Expense savedExpense = expenseService.addExpense(mockExpense);

        // Assert
        assertNotNull(savedExpense);
        assertEquals(1L, savedExpense.getExpenseId());
        assertEquals(new BigDecimal("1500.00"), savedExpense.getAmount());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void getExpenseById_Success() {
        // Arrange
        when(expenseRepository.findByExpenseId(1L)).thenReturn(Optional.of(mockExpense));

        // Act
        Expense foundExpense = expenseService.getExpenseById(1L);

        // Assert
        assertNotNull(foundExpense);
        assertEquals("Groceries", foundExpense.getTitle());
    }

    @Test
    void getExpenseById_NotFound_ThrowsException() {
        // Arrange
        when(expenseRepository.findByExpenseId(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.getExpenseById(99L);
        });
        assertEquals("Expense not found with ID: 99", exception.getMessage());
    }

    @Test
    void getTotalByUser_WithExpenses_ReturnsTotal() {
        // Arrange
        when(expenseRepository.sumAmountByUserId(1)).thenReturn(new BigDecimal("4500.50"));

        // Act
        BigDecimal total = expenseService.getTotalByUser(1);

        // Assert
        assertEquals(new BigDecimal("4500.50"), total);
    }

    @Test
    void getTotalByUser_NoExpenses_ReturnsZero() {
        // Arrange
        when(expenseRepository.sumAmountByUserId(1)).thenReturn(null);

        // Act
        BigDecimal total = expenseService.getTotalByUser(1);

        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }
}