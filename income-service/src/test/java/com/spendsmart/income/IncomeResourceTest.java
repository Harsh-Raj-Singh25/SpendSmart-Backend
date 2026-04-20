package com.spendsmart.income;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.model.enums.IncomeSource;
import com.spendsmart.income.resource.IncomeResource;
import com.spendsmart.income.service.IncomeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeResourceTest {

    @Mock
    private IncomeService incomeService;

    @InjectMocks
    private IncomeResource incomeResource;

    @Test
    void allEndpointsDelegateToService() {
        Income income = Income.builder()
                .incomeId(1)
                .userId(5)
                .categoryId(2)
                .title("Salary")
                .amount(new BigDecimal("75000.00"))
                .source(IncomeSource.SALARY)
                .date(LocalDate.of(2026, 4, 20))
                .isRecurring(true)
                .build();

        when(incomeService.addIncome(income)).thenReturn(income);
        assertEquals(201, incomeResource.addIncome(income).getStatusCode().value());

        when(incomeService.getIncomeById(1)).thenReturn(income);
        assertEquals(1, incomeResource.getIncomeById(1).getBody().getIncomeId());

        when(incomeService.getIncomesByUser(5)).thenReturn(List.of(income));
        assertEquals(1, incomeResource.getIncomesByUser(5).getBody().size());

        when(incomeService.getIncomesBySource(5, IncomeSource.SALARY)).thenReturn(List.of(income));
        assertEquals(1, incomeResource.getIncomesBySource(5, IncomeSource.SALARY).getBody().size());

        when(incomeService.getIncomesByDateRange(5, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
                .thenReturn(List.of(income));
        assertEquals(1,
                incomeResource.getIncomesByDateRange(5, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30))
                        .getBody().size());

        when(incomeService.getIncomesByMonth(5, 2026, 4)).thenReturn(List.of(income));
        assertEquals(1, incomeResource.getIncomesByMonth(5, 2026, 4).getBody().size());

        when(incomeService.getRecurringIncomes(5)).thenReturn(List.of(income));
        assertEquals(1, incomeResource.getRecurringIncomes(5).getBody().size());

        when(incomeService.updateIncome(1, income)).thenReturn(income);
        assertEquals(1, incomeResource.updateIncome(1, income).getBody().getIncomeId());

        assertEquals(204, incomeResource.deleteIncome(1).getStatusCode().value());

        when(incomeService.getTotalIncomeByUser(5)).thenReturn(new BigDecimal("100000.00"));
        assertEquals(new BigDecimal("100000.00"), incomeResource.getTotalIncomeByUser(5).getBody());

        when(incomeService.getTotalIncomeByMonth(5, 2026, 4)).thenReturn(new BigDecimal("75000.00"));
        assertEquals(new BigDecimal("75000.00"), incomeResource.getTotalIncomeByMonth(5, 2026, 4).getBody());

        when(incomeService.getTotalIncomeByYear(5, 2026)).thenReturn(new BigDecimal("900000.00"));
        assertEquals(new BigDecimal("900000.00"), incomeResource.getTotalIncomeByYear(5, 2026).getBody());

        verify(incomeService).deleteIncome(1);
    }
}
