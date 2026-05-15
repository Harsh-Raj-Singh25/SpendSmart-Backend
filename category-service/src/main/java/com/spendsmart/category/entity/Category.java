package com.spendsmart.category.entity;

import com.spendsmart.category.model.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(nullable = false)
    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @Column(nullable = false)
    @NotBlank(message = "Category name cannot be blank")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Category type cannot be null")
    private CategoryType type;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    private String colorCode; // e.g., "#FF5733"

    @Column(precision = 12, scale = 2)
    private BigDecimal budgetLimit;

    @Column(nullable = false)
    private Boolean isDefault;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate createdAt;
}