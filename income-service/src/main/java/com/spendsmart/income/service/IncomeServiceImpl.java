package com.spendsmart.income.service;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.model.enums.IncomeSource;
import com.spendsmart.income.repository.IncomeRepository; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncomeServiceImpl implements IncomeService {

	private final IncomeRepository incomeRepository;

	@Override
	public Income addIncome(Income income) {
		log.info("Adding new income for user: {}", income.getUserId());
		return incomeRepository.save(income);
	}

	@Override
	@Transactional(readOnly = true)
	public Income getIncomeById(Integer incomeId) {
		return incomeRepository.findByIncomeId(incomeId)
				.orElseThrow(() -> new RuntimeException("Income not found with ID: " + incomeId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Income> getIncomesByUser(Integer userId) {
		return incomeRepository.findByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Income> getIncomesBySource(Integer userId, IncomeSource source) {
		return incomeRepository.findByUserIdAndSource(userId, source);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Income> getIncomesByDateRange(Integer userId, LocalDate startDate, LocalDate endDate) {
		return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Income> getIncomesByMonth(Integer userId, int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		return getIncomesByDateRange(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
	}

	@Override
	public Income updateIncome(Integer incomeId, Income incomeDetails) {
		log.info("Updating income ID: {}", incomeId);
		Income existingIncome = getIncomeById(incomeId);

		existingIncome.setCategoryId(incomeDetails.getCategoryId());
		existingIncome.setTitle(incomeDetails.getTitle());
		existingIncome.setAmount(incomeDetails.getAmount());
		existingIncome.setCurrency(incomeDetails.getCurrency());
		existingIncome.setSource(incomeDetails.getSource());
		existingIncome.setDate(incomeDetails.getDate());
		existingIncome.setNotes(incomeDetails.getNotes());
		existingIncome.setIsRecurring(incomeDetails.getIsRecurring());
		existingIncome.setRecurrencePeriod(incomeDetails.getRecurrencePeriod());

		return incomeRepository.save(existingIncome);
	}

	@Override
	public void deleteIncome(Integer incomeId) {
		log.info("Deleting income ID: {}", incomeId);
		getIncomeById(incomeId);
		incomeRepository.deleteByIncomeId(incomeId);
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalIncomeByUser(Integer userId) {
		BigDecimal total = incomeRepository.sumAmountByUserId(userId);
		return total != null ? total : BigDecimal.ZERO;
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalIncomeByMonth(Integer userId, int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		BigDecimal total = incomeRepository.sumAmountByUserIdAndDateBetween(userId, yearMonth.atDay(1),
				yearMonth.atEndOfMonth());
		return total != null ? total : BigDecimal.ZERO;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Income> getRecurringIncomes(Integer userId) {
		return incomeRepository.findByUserIdAndIsRecurringTrue(userId);
	}
}