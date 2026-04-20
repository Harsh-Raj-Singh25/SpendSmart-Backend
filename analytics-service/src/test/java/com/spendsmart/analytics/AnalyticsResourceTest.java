package com.spendsmart.analytics;

import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.model.dto.MonthlySummary;
import com.spendsmart.analytics.model.dto.SnapshotDto;
import com.spendsmart.analytics.model.dto.YearlySummary;
import com.spendsmart.analytics.resource.AnalyticsResource;
import com.spendsmart.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsResourceTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsResource analyticsResource;

    @Test
    void allEndpointsDelegateToService() {
        FinancialSnapshot snapshot = FinancialSnapshot.builder().snapshotId(1).userId(5).build();
        MonthlySummary monthlySummary = MonthlySummary.builder().year(2026).month(4).build();
        YearlySummary yearlySummary = YearlySummary.builder().year(2026).build();
        SnapshotDto snapshotDto = SnapshotDto.builder().topCategory("Food").build();

        when(analyticsService.generateMonthlySnapshot(5, 2026, 4)).thenReturn(snapshot);
        assertEquals(1, analyticsResource.generateSnapshot(5, 2026, 4).getBody().getSnapshotId());

        when(analyticsService.getFinancialHealthScore(5)).thenReturn(78);
        assertEquals(78, analyticsResource.getHealthScore(5).getBody());

        when(analyticsService.getSpendingForecast(5)).thenReturn(new BigDecimal("23000.00"));
        assertEquals(new BigDecimal("23000.00"), analyticsResource.getForecast(5).getBody());

        when(analyticsService.getMonthlySummary(5, 2026, 4)).thenReturn(monthlySummary);
        assertEquals(4, analyticsResource.getMonthlySummary(5, 2026, 4).getBody().getMonth());

        when(analyticsService.getYearlySummary(5, 2026)).thenReturn(yearlySummary);
        assertEquals(2026, analyticsResource.getYearlySummary(5, 2026).getBody().getYear());

        when(analyticsService.getExpenseBreakdownByCategory(5, 2026, 4))
                .thenReturn(Map.of("Food", new BigDecimal("800.00")));
        assertEquals(new BigDecimal("800.00"), analyticsResource.getCategoryBreakdown(5, 2026, 4).getBody().get("Food"));

        when(analyticsService.getIncomeVsExpenseTrend(5, 2026)).thenReturn(List.of(Map.of("month", "APR")));
        assertEquals(1, analyticsResource.getIncomeVsExpenseTrend(5, 2026).getBody().size());

        when(analyticsService.getSavingsRateTrend(5, 2026)).thenReturn(List.of(Map.of("rate", 30)));
        assertEquals(1, analyticsResource.getSavingsRateTrend(5, 2026).getBody().size());

        when(analyticsService.getTopSpendingCategories(5, 4)).thenReturn(List.of(Map.entry("Food", new BigDecimal("800.00"))));
        assertEquals(1, analyticsResource.getTopCategories(5, 4).getBody().size());

        when(analyticsService.getDailyExpenseTrend(5, 2026, 4)).thenReturn(List.of(Map.of("day", 20)));
        assertEquals(1, analyticsResource.getDailyTrend(5, 2026, 4).getBody().size());

        when(analyticsService.getCashflowData(5, 4)).thenReturn(Map.of("income", new BigDecimal("1000.00")));
        assertEquals(new BigDecimal("1000.00"), analyticsResource.getCashflow(5, 4).getBody().get("income"));

        when(analyticsService.getMonthlySnapshotDto(5, 4, 2026)).thenReturn(snapshotDto);
        assertEquals("Food", analyticsResource.getMonthlySnapshot(5, 4, 2026).getBody().getTopCategory());

        when(analyticsService.calculateCategorySpending(5, 4, 2026)).thenReturn(Map.of("Food", 80.0));
        assertEquals(80.0, analyticsResource.getCategoryPieChart(5, 4, 2026).getBody().get("Food"));

        when(analyticsService.calculate6MonthCashflow(5, 4, 2026)).thenReturn(Map.of("months", List.of("Jan")));
        assertEquals(1, ((List<?>) analyticsResource.getCashflowData(5, 4, 2026).getBody().get("months")).size());

        when(analyticsService.calculateHealthScore(5, 4)).thenReturn(82);
        assertEquals(82, analyticsResource.getFinancialHealthScore(5, 4).getBody());
    }
}
