package com.spendsmart.income.repository;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.model.enums.IncomeSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer> {

    List<Income> findByUserId(Integer userId);
    
    List<Income> findByUserIdAndSource(Integer userId, IncomeSource source);
    
    List<Income> findByUserIdAndDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);
    
    List<Income> findByUserIdAndIsRecurringTrue(Integer userId);
    
    Optional<Income> findByIncomeId(Integer incomeId);
    
    void deleteByIncomeId(Integer incomeId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Integer userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateBetween(
            @Param("userId") Integer userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate
    );
}