package com.spendsmart.web.clients;
 
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.spendsmart.web.dto.SnapshotDto;

@FeignClient(name = "analytics-client", url = "http://localhost:8080/analytics")
public interface AnalyticsClient {
	@GetMapping("/snapshot/user/{userId}/month/{month}/year/{year}")
	SnapshotDto getMonthlySnapshot(@PathVariable("userId") int userId, @PathVariable("month") int month,
			@PathVariable("year") int year);

	@GetMapping("/charts/category-pie")
	Map<String, Double> getCategoryPieChart(@RequestParam("userId") int userId, @RequestParam("month") int month,
			@RequestParam("year") int year);

	@GetMapping("/charts/cashflow")
	Map<String, Object> getCashflowData(@RequestParam("userId") int userId, @RequestParam("month") int month,
			@RequestParam("year") int year);

	@GetMapping("/health-score")
	Integer getFinancialHealthScore(@RequestParam("userId") int userId, @RequestParam("month") int month);
}