package com.spendsmart.category.service;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.model.enums.CategoryType;
import com.spendsmart.category.repository.CategoryRepository; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	@Override
	public Category createCategory(Category category) {
		log.info("Creating new category for user: {}", category.getUserId());

		// Ensure non-default categories are marked explicitly to avoid DB null constraint
		if (category.getIsDefault() == null) {
			category.setIsDefault(false);
		}
		// Prevent duplicate names for the same user
		if (categoryRepository.findByUserIdAndName(category.getUserId(), category.getName()).isPresent()) {
			throw new RuntimeException("Category with this name already exists for the user.");
		}
		return categoryRepository.save(category);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Category> getByUserId(Integer userId) {
		return categoryRepository.findByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public Category getCategoryById(Integer categoryId) {
		return categoryRepository.findByCategoryId(categoryId)
				.orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Category> getByUserAndType(Integer userId, CategoryType type) {
		return categoryRepository.findByUserIdAndType(userId, type);
	}

	@Override
	public Category updateCategory(Integer categoryId, Category categoryDetails) {
		log.info("Updating category ID: {}", categoryId);
		Category existingCategory = getCategoryById(categoryId);

		existingCategory.setName(categoryDetails.getName());
		existingCategory.setIcon(categoryDetails.getIcon());
		existingCategory.setColorCode(categoryDetails.getColorCode());
		existingCategory.setType(categoryDetails.getType());

		return categoryRepository.save(existingCategory);
	}

	@Override
	public void deleteCategory(Integer categoryId) {
		log.info("Deleting category ID: {}", categoryId);
		getCategoryById(categoryId); // Verify it exists
		categoryRepository.deleteByCategoryId(categoryId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Category> getDefaultCategories() {
		return categoryRepository.findByIsDefaultTrue();
	}

	@Override
	public void initDefaultCategories(Integer userId) {
		log.info("Initializing default categories for user: {}", userId);

		// Skip if user already has categories to prevent duplicate seeding
		if (categoryRepository.countByUserId(userId) > 0) {
			log.info("User already has categories. Skipping initialization.");
			return;
		}

		List<Category> defaultCategories = List.of(
				Category.builder().userId(userId).name("Food & Dining").type(CategoryType.EXPENSE).icon("🍔")
						.colorCode("#FF5733").isDefault(true).build(),
				Category.builder().userId(userId).name("Transport").type(CategoryType.EXPENSE).icon("🚗")
						.colorCode("#33A1FF").isDefault(true).build(),
				Category.builder().userId(userId).name("Bills & Utilities").type(CategoryType.EXPENSE).icon("💡")
						.colorCode("#FFC300").isDefault(true).build(),
				Category.builder().userId(userId).name("Health & Fitness").type(CategoryType.EXPENSE).icon("💊")
						.colorCode("#E333FF").isDefault(true).build(),
				Category.builder().userId(userId).name("Salary").type(CategoryType.INCOME).icon("💰")
						.colorCode("#28A745").isDefault(true).build(),
				Category.builder().userId(userId).name("Investments").type(CategoryType.INCOME).icon("📈")
						.colorCode("#17A2B8").isDefault(true).build());

		categoryRepository.saveAll(defaultCategories);
	}

	@Override
	public void setCategoryBudget(Integer categoryId, BigDecimal budgetLimit) {
		log.info("Setting budget limit for category ID: {}", categoryId);
		Category existingCategory = getCategoryById(categoryId);
		existingCategory.setBudgetLimit(budgetLimit);
		categoryRepository.save(existingCategory);
	}

	@Override
	@Transactional(readOnly = true)
	public int getCategoryCount(Integer userId) {
		return categoryRepository.countByUserId(userId);
	}
}