package com.spendsmart.category.service;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.model.enums.CategoryType;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryService {
	Category createCategory(Category category);

	List<Category> getAllCategories();

	List<Category> getByUserId(Integer userId);

	Category getCategoryById(Integer categoryId);

	List<Category> getByUserAndType(Integer userId, CategoryType type);

	Category updateCategory(Integer categoryId, Category categoryDetails);

	void deleteCategory(Integer categoryId);

	List<Category> getDefaultCategories();

	void initDefaultCategories(Integer userId);

	void setCategoryBudget(Integer categoryId, BigDecimal budgetLimit);

	int getCategoryCount(Integer userId);
}