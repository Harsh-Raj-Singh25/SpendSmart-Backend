package com.spendsmart.category.entity;

import com.spendsmart.category.model.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private Integer userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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