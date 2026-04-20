package com.spendsmart.expense;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;
import com.spendsmart.expense.model.enums.PaymentMethod;
import com.spendsmart.expense.resource.ExpenseResource;
import com.spendsmart.expense.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseResourceTest {

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseResource expenseResource;

    @Test
    void allEndpointsDelegateToService() {
        Expense expense = Expense.builder()
                .expenseId(1L)
                .userId(5)
                .categoryId(10)
                .title("Lunch")
                .amount(new BigDecimal("250.00"))
                .type(ExpenseType.EXPENSE)
                .paymentMethod(PaymentMethod.CASH)
                .date(LocalDate.of(2026, 4, 20))
                .isRecurring(false)
                .build();

        when(expenseService.addExpense(expense)).thenReturn(expense);
        assertEquals(201, expenseResource.addExpense(expense).getStatusCode().value());

        when(expenseService.getExpenseById(1L)).thenReturn(expense);
        assertEquals(1L, expenseResource.getExpenseById(1L).getBody().getExpenseId());

        when(expenseService.getExpensesByUser(5)).thenReturn(List.of(expense));
        assertEquals(1, expenseResource.getExpensesByUser(5).getBody().size());

        when(expenseService.getExpensesByCategory(10)).thenReturn(List.of(expense));
        assertEquals(1, expenseResource.getExpensesByCategory(10).getBody().size());

        when(expenseService.getExpensesByDateRange(5, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
                .thenReturn(List.of(expense));
        assertEquals(1, expenseResource.getExpensesByDateRange(5, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)).getBody().size());

        when(expenseService.getExpensesByType(5, ExpenseType.EXPENSE)).thenReturn(List.of(expense));
        assertEquals(1, expenseResource.getExpensesByType(5, ExpenseType.EXPENSE).getBody().size());

        when(expenseService.searchExpenses(5, "lunch")).thenReturn(List.of(expense));
        assertEquals(1, expenseResource.searchExpenses(5, "lunch").getBody().size());

        when(expenseService.updateExpense(1L, expense)).thenReturn(expense);
        assertEquals(1L, expenseResource.updateExpense(1L, expense).getBody().getExpenseId());

        assertEquals(204, expenseResource.deleteExpense(1L).getStatusCode().value());

        when(expenseService.getTotalByUser(5)).thenReturn(new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("1000.00"), expenseResource.getTotalByUser(5).getBody());

        when(expenseService.getTotalByCategory(10)).thenReturn(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("500.00"), expenseResource.getTotalByCategory(10).getBody());

        when(expenseService.getTotalExpenseByYear(5, 2026)).thenReturn(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("5000.00"), expenseResource.getTotalExpenseByYear(5, 2026).getBody());

        when(expenseService.getExpenseBreakdownByCategory(5, 2026, 4))
                .thenReturn(Map.of("Food", new BigDecimal("800.00")));
        assertEquals(new BigDecimal("800.00"),
                expenseResource.getExpenseBreakdownByCategory(5, 2026, 4).getBody().get("Food"));

        when(expenseService.getDailyExpenseTrend(5, 2026, 4)).thenReturn(Map.of(20, new BigDecimal("250.00")));
        assertEquals(new BigDecimal("250.00"), expenseResource.getDailyExpenseTrend(5, 2026, 4).getBody().get(20));

        when(expenseService.getTotalExpenseByMonth(5, 2026, 4)).thenReturn(new BigDecimal("1200.00"));
        assertEquals(new BigDecimal("1200.00"), expenseResource.getTotalExpenseByMonth(5, 2026, 4).getBody());

        verify(expenseService).deleteExpense(1L);
    }
}
