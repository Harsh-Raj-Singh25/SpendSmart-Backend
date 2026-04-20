package com.spendsmart.web;

import com.spendsmart.web.clients.*;
import com.spendsmart.web.controller.ExpenseController;
import com.spendsmart.web.dto.CategoryDto;
import com.spendsmart.web.dto.SnapshotDto;
import com.spendsmart.web.dto.TransactionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseClient expenseClient;

    @Mock
    private IncomeClient incomeClient;

    @Mock
    private AnalyticsClient analyticsClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private CategoryClient categoryClient;

    @InjectMocks
    private ExpenseController expenseController;

    @Test
    void viewDashboard_SuccessAndErrorPaths() {
        SnapshotDto snapshot = new SnapshotDto();
        snapshot.setTotalIncome(new BigDecimal("50000.00"));
        snapshot.setTotalExpenses(new BigDecimal("20000.00"));
        snapshot.setNetSavings(new BigDecimal("30000.00"));

        TransactionDto transaction = new TransactionDto();
        transaction.setTitle("Lunch");

        when(analyticsClient.getMonthlySnapshot(eq(5), anyInt(), anyInt())).thenReturn(snapshot);
        when(expenseClient.getUserExpenses(5)).thenReturn(List.of(transaction));
        when(notificationClient.getUnreadCount(5)).thenReturn(2);

        ModelAndView ok = expenseController.viewDashboard();
        assertEquals("dashboard", ok.getViewName());
        assertEquals(new BigDecimal("50000.00"), ok.getModel().get("totalIncome"));
        assertEquals(2, ok.getModel().get("unreadCount"));

        when(analyticsClient.getMonthlySnapshot(eq(5), anyInt(), anyInt())).thenThrow(new RuntimeException("down"));
        ModelAndView error = expenseController.viewDashboard();
        assertEquals("dashboard", error.getViewName());
        assertTrue(error.getModel().containsKey("error"));
    }

    @Test
    void formAndViewEndpointsWorkAsExpected() {
        TransactionDto expenseForm = new TransactionDto();
        expenseForm.setTitle("Coffee");

        String redirect = expenseController.addExpense(expenseForm);
        assertEquals("redirect:/app/dashboard", redirect);

        ArgumentCaptor<TransactionDto> dtoCaptor = ArgumentCaptor.forClass(TransactionDto.class);
        verify(expenseClient).addExpense(dtoCaptor.capture());
        assertEquals(5, dtoCaptor.getValue().getUserId());
        assertEquals("EXPENSE", dtoCaptor.getValue().getType());

        assertEquals("index", expenseController.home());

        Model model = new ExtendedModelMap();
        assertEquals("register", expenseController.register(model));
        assertTrue(model.containsAttribute("user"));

        assertEquals("redirect:/app/dashboard", expenseController.login(new ExtendedModelMap(), "u", "p", null));

        assertEquals("profile", expenseController.viewProfile().getViewName());
        assertEquals("redirect:/app/profile", expenseController.editProfile(new Object()));

        assertEquals("expense-form", expenseController.editExpense(1).getViewName());
        assertEquals("redirect:/app/expenses", expenseController.updateExpense(1, new Object()).getViewName());
        assertEquals(200, expenseController.deleteExpense(1).getStatusCode().value());
        assertEquals("expense-list", expenseController.viewExpenses().getViewName());
        assertEquals("expense-list", expenseController.filterExpenses(Map.of("month", "4")).getViewName());

        assertEquals("income-form", expenseController.addIncome().getViewName());
        assertEquals("income-form", expenseController.editIncome(1).getViewName());
        assertEquals("redirect:/app/incomes", expenseController.updateIncome().getViewName());
        assertEquals("redirect:/app/incomes", expenseController.deleteIncome(1).getViewName());
        assertEquals("income-list", expenseController.viewIncomes().getViewName());

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Travel");
        when(categoryClient.getAllCategories()).thenReturn(List.of(categoryDto));

        ModelAndView categories = expenseController.manageCategories();
        assertEquals("categories", categories.getViewName());
        assertEquals(1, ((List<?>) categories.getModel().get("categories")).size());

        assertEquals("redirect:/app/categories", expenseController.addCategory(categoryDto));
        verify(categoryClient).addCategory(categoryDto);

        assertEquals("redirect:/app/dashboard", expenseController.setBudget().getViewName());
        assertEquals("notifications", expenseController.viewNotifications().getViewName());
        assertEquals("redirect:/app/login", expenseController.logout());
    }
}
