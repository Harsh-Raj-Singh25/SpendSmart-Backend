package com.spendsmart.analytics.repository;

import com.spendsmart.analytics.entity.FinancialSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<FinancialSnapshot, Integer> {

	//  Standard Derived Queries
	List<FinancialSnapshot> findByUserId(Integer userId);

	Optional<FinancialSnapshot> findByUserIdAndYearAndMonth(Integer userId, Integer year, Integer month);

	List<FinancialSnapshot> findByUserIdAndYear(Integer userId, Integer year);

	int countByUserId(Integer userId);

	// 2. Custom JPQL Aggregation (Added for the Case Study)
	// Calculates the all-time average savings rate for a specific user
	@Query("SELECT AVG(f.savingsRate) FROM FinancialSnapshot f WHERE f.userId = :userId")
	Double getAverageSavingsRateByUserId(@Param("userId") Integer userId);

	//  Derived Sorting/Limiting Query (Added for the Case Study)
	// Finds the top 3 months where the user spent the most money
	List<FinancialSnapshot> findTop3ByUserIdOrderByTotalExpensesDesc(Integer userId);
}