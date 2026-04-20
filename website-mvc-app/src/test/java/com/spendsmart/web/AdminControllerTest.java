package com.spendsmart.web;

import com.spendsmart.web.clients.AnalyticsClient;
import com.spendsmart.web.clients.AuthClient;
import com.spendsmart.web.clients.ExpenseClient;
import com.spendsmart.web.clients.NotificationClient;
import com.spendsmart.web.controller.AdminController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private ExpenseClient expenseClient;

    @Mock
    private AnalyticsClient analyticsClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AdminController adminController;

    @Test
    void allEndpointsReturnExpectedViewsOrRedirects() {
        assertEquals("admin/dashboard", adminController.adminDashboard().getViewName());
        assertEquals("admin/users", adminController.manageAllUsers().getViewName());
        assertEquals("redirect:/admin/users", adminController.suspendUser(1));
        assertEquals("redirect:/admin/users", adminController.deleteUser(1));

        assertEquals("admin/all-expenses", adminController.viewAllExpenses().getViewName());
        assertEquals("admin/all-incomes", adminController.viewAllIncomes().getViewName());
        assertEquals("admin/platform-analytics", adminController.viewPlatformAnalytics().getViewName());
        assertEquals("admin/total-spending", adminController.viewTotalSpending().getViewName());
        assertEquals("admin/top-users", adminController.viewTopSpendingUsers().getViewName());
        assertEquals("admin/category-usage", adminController.viewCategoryUsage().getViewName());

        assertEquals("redirect:/admin/dashboard?alertSent=true", adminController.sendPlatformNotification("Title", "Body"));
        assertEquals("admin/system-alerts", adminController.viewBudgetAlerts().getViewName());
        assertEquals("admin/report-viewer", adminController.generatePlatformReport().getViewName());
        assertEquals("admin/audit-logs", adminController.viewAuditLogs().getViewName());
    }
}
