package com.spendsmart.category.repository;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.model.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByUserId(Integer userId);
    
    List<Category> findByUserIdAndType(Integer userId, CategoryType type);
    
    Optional<Category> findByCategoryId(Integer categoryId);
    
    Optional<Category> findByUserIdAndName(Integer userId, String name);
    
    List<Category> findByIsDefaultTrue();
    
    int countByUserId(Integer userId);
    
    void deleteByUserId(Integer userId);
    
    void deleteByCategoryId(Integer categoryId);
}