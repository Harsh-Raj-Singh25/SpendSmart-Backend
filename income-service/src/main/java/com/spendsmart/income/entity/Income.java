package com.spendsmart.income.entity;

import com.spendsmart.income.model.enums.IncomeSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "incomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer incomeId;

    @Column(nullable = false)
    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @Column(nullable = false)
    @NotNull(message = "Category ID cannot be null")
    private Integer categoryId;

    @Column(nullable = false)
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    @NotBlank(message = "Currency cannot be blank")
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Source cannot be null")
    private IncomeSource source;

    @Column(nullable = false)
    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean isRecurring;

    private String recurrencePeriod;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}