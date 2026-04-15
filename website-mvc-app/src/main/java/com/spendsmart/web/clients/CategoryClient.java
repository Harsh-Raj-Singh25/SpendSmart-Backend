package com.spendsmart.web.clients;

import com.spendsmart.web.dto.CategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "category-client", url = "http://localhost:8080/categories")
public interface CategoryClient {

	// Assuming categories are global or fetched by user depending on your backend
	// setup
	@GetMapping
	List<CategoryDto> getAllCategories();

	@PostMapping
	CategoryDto addCategory(@RequestBody CategoryDto category);

	@DeleteMapping("/{id}")
	void deleteCategory(@PathVariable("id") int id);
}