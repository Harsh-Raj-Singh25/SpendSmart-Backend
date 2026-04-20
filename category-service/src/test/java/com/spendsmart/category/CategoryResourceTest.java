package com.spendsmart.category;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.model.enums.CategoryType;
import com.spendsmart.category.resource.CategoryResource;
import com.spendsmart.category.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryResourceTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryResource categoryResource;

    @Test
    void allEndpointsDelegateToService() {
        Category category = Category.builder()
                .categoryId(1)
                .userId(5)
                .name("Food")
                .type(CategoryType.EXPENSE)
                .budgetLimit(new BigDecimal("5000.00"))
                .isDefault(false)
                .build();

        when(categoryService.createCategory(category)).thenReturn(category);
        assertEquals(201, categoryResource.createCategory(category).getStatusCode().value());

        when(categoryService.getByUserId(5)).thenReturn(List.of(category));
        assertEquals(1, categoryResource.getByUserId(5).getBody().size());

        when(categoryService.getCategoryById(1)).thenReturn(category);
        assertEquals("Food", categoryResource.getById(1).getBody().getName());

        when(categoryService.getByUserAndType(5, CategoryType.EXPENSE)).thenReturn(List.of(category));
        assertEquals(1, categoryResource.getByType(5, CategoryType.EXPENSE).getBody().size());

        when(categoryService.getDefaultCategories()).thenReturn(List.of(category));
        assertEquals(1, categoryResource.getDefaults().getBody().size());

        assertEquals(201, categoryResource.initDefaults(5).getStatusCode().value());
        verify(categoryService).initDefaultCategories(5);

        when(categoryService.updateCategory(1, category)).thenReturn(category);
        assertEquals("Food", categoryResource.update(1, category).getBody().getName());

        assertEquals(200, categoryResource.setBudget(1, new BigDecimal("3000.00")).getStatusCode().value());
        verify(categoryService).setCategoryBudget(1, new BigDecimal("3000.00"));

        assertEquals(204, categoryResource.delete(1).getStatusCode().value());
        verify(categoryService).deleteCategory(1);

        when(categoryService.getCategoryCount(5)).thenReturn(4);
        assertEquals(4, categoryResource.getCount(5).getBody());
    }
}
