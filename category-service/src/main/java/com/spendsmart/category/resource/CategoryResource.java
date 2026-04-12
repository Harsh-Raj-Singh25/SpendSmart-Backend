package com.spendsmart.category.resource;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.model.enums.CategoryType;
import com.spendsmart.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryResource {

	private final CategoryService categoryService;

	@PostMapping
	public ResponseEntity<Category> createCategory(@RequestBody Category category) {
		return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Category>> getByUserId(@PathVariable Integer userId) {
		return ResponseEntity.ok(categoryService.getByUserId(userId));
	}

	@GetMapping("/{categoryId}")
	public ResponseEntity<Category> getById(@PathVariable Integer categoryId) {
		return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
	}

	@GetMapping("/user/{userId}/type")
	public ResponseEntity<List<Category>> getByType(@PathVariable Integer userId, @RequestParam CategoryType type) {
		return ResponseEntity.ok(categoryService.getByUserAndType(userId, type));
	}

	@GetMapping("/defaults")
	public ResponseEntity<List<Category>> getDefaults() {
		return ResponseEntity.ok(categoryService.getDefaultCategories());
	}

	@PostMapping("/user/{userId}/initDefaults")
	public ResponseEntity<Void> initDefaults(@PathVariable Integer userId) {
		categoryService.initDefaultCategories(userId);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{categoryId}")
	public ResponseEntity<Category> update(@PathVariable Integer categoryId, @RequestBody Category category) {
		return ResponseEntity.ok(categoryService.updateCategory(categoryId, category));
	}

	@PatchMapping("/{categoryId}/budget")
	public ResponseEntity<Void> setBudget(@PathVariable Integer categoryId, @RequestParam BigDecimal budgetLimit) {
		categoryService.setCategoryBudget(categoryId, budgetLimit);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{categoryId}")
	public ResponseEntity<Void> delete(@PathVariable Integer categoryId) {
		categoryService.deleteCategory(categoryId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/user/{userId}/count")
	public ResponseEntity<Integer> getCount(@PathVariable Integer userId) {
		return ResponseEntity.ok(categoryService.getCategoryCount(userId));
	}
}