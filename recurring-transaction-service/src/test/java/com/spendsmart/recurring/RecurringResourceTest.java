package com.spendsmart.recurring;

import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.model.enums.Frequency;
import com.spendsmart.recurring.model.enums.TransactionType;
import com.spendsmart.recurring.resource.RecurringResource;
import com.spendsmart.recurring.service.RecurringService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringResourceTest {

    @Mock
    private RecurringService recurringService;

    @InjectMocks
    private RecurringResource recurringResource;

    @Test
    void allEndpointsDelegateToService() {
        RecurringTransaction recurring = RecurringTransaction.builder()
                .recurringId(1)
                .userId(5)
                .categoryId(2)
                .title("Rent")
                .amount(new BigDecimal("15000.00"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.of(2026, 4, 1))
                .nextDueDate(LocalDate.of(2026, 5, 1))
                .isActive(true)
                .paymentMethod("UPI")
                .build();

        when(recurringService.addRecurring(recurring)).thenReturn(recurring);
        assertEquals(201, recurringResource.add(recurring).getStatusCode().value());

        when(recurringService.getByUser(5)).thenReturn(List.of(recurring));
        assertEquals(1, recurringResource.getByUser(5).getBody().size());

        when(recurringService.getById(1)).thenReturn(Optional.of(recurring));
        assertEquals(200, recurringResource.getById(1).getStatusCode().value());

        when(recurringService.getById(99)).thenReturn(Optional.empty());
        assertEquals(404, recurringResource.getById(99).getStatusCode().value());

        when(recurringService.getActiveRecurring(5)).thenReturn(List.of(recurring));
        assertEquals(1, recurringResource.getActive(5).getBody().size());

        when(recurringService.getUpcomingThisMonth(5)).thenReturn(List.of(recurring));
        assertEquals(1, recurringResource.getUpcomingThisMonth(5).getBody().size());

        when(recurringService.updateRecurring(1, recurring)).thenReturn(recurring);
        assertEquals(1, recurringResource.update(1, recurring).getBody().getRecurringId());

        assertEquals(200, recurringResource.deactivate(1).getStatusCode().value());
        verify(recurringService).deactivateRecurring(1);

        assertEquals(204, recurringResource.delete(1).getStatusCode().value());
        verify(recurringService).deleteRecurring(1);
    }
}
