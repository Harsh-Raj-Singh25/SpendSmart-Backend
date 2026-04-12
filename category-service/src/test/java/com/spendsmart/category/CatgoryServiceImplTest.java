package com.spendsmart.category;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.model.enums.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import com.spendsmart.category.service.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category mockCategory;

    @BeforeEach
    void setUp() {
        mockCategory = Category.builder()
                .categoryId(1)
                .userId(1)
                .name("Travel")
                .type(CategoryType.EXPENSE)
                .icon("✈️")
                .colorCode("#0000FF")
                .isDefault(false)
                .build();
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.findByUserIdAndName(1, "Travel")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

        Category saved = categoryService.createCategory(mockCategory);

        assertNotNull(saved);
        assertEquals("Travel", saved.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        when(categoryRepository.findByUserIdAndName(1, "Travel")).thenReturn(Optional.of(mockCategory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.createCategory(mockCategory);
        });

        assertEquals("Category with this name already exists for the user.", exception.getMessage());
    }

    @Test
    void initDefaultCategories_SeedsOnlyIfEmpty() {
        when(categoryRepository.countByUserId(1)).thenReturn(0);

        categoryService.initDefaultCategories(1);

        verify(categoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void setCategoryBudget_UpdatesCorrectly() {
        when(categoryRepository.findByCategoryId(1)).thenReturn(Optional.of(mockCategory));
        
        categoryService.setCategoryBudget(1, new BigDecimal("5000.00"));

        assertEquals(new BigDecimal("5000.00"), mockCategory.getBudgetLimit());
        verify(categoryRepository, times(1)).save(mockCategory);
    }
}