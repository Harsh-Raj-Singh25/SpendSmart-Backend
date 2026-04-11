package com.spendsmart.expense.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>{
	
	List<Expense> findByUserId(Integer userId);
	
	List<Expense> findByUserIdAndType(Integer userId, ExpenseType type);
	
	List<Expense> findByCategoryId(Long categoryId);
	
	List<Expense> findByUserIdAndDate(Integer userId, LocalDate date);

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);
	
	Optional<Expense> findByExpenseId(Long expenseId); //n older versions of Java, if a database didn't find a record, it would return null.
	//If you tried to call .getAmount() on a null object, your application would crash with the dreaded NullPointerException.
	//Optional<Expense> acts as a safe "box" around the result.
	
	void deleteByExpenseId(Long expenseId);
	
	// Custom JPQL to handle the total aggregation required by the document
	@Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
	BigDecimal sumAmountByUserId(@Param("userId")Integer userId);
	
	@Query("SELECT SUM(e.amount) FROM Expense e WHERE e.categoryId = :categoryId")
    BigDecimal sumAmountByCategoryId(@Param("categoryId") Long categoryId);

    // Custom query for the keyword search across title and notes
    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Expense> searchExpensesByKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword);

}
