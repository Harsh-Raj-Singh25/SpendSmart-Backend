package com.spendsmart.budget.repository;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.enums.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

	List<Budget> findByUserId(Integer userId);

	Optional<Budget> findByBudgetId(Integer budgetId);

	Optional<Budget> findByUserIdAndCategoryId(Integer userId, Integer categoryId);

	List<Budget> findByPeriod(BudgetPeriod period);

	List<Budget> findByIsActive(Boolean isActive);

	List<Budget> findByUserIdAndIsActive(Integer userId, Boolean isActive);

	int countByUserId(Integer userId);

    void deleteByBudgetId(Integer budgetId);
}