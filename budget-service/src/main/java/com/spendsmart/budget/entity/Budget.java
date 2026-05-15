package com.spendsmart.budget.entity;

import com.spendsmart.budget.model.enums.BudgetPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer budgetId;

	@Column(nullable = false)
	@NotNull(message = "User ID cannot be null")
	private Integer userId;

	@Column(nullable = false)
	@NotNull(message = "Category ID cannot be null")
	private Integer categoryId;

	@Column(nullable = false)
	@NotBlank(message = "Name cannot be blank")
	private String name;

	@Column(nullable = false, precision = 12, scale = 2)
	@NotNull(message = "Limit amount cannot be null")
	@Positive(message = "Limit amount must be positive")
	private BigDecimal limitAmount;

	@Column(length = 3)
	@Builder.Default
	@NotBlank(message = "Currency cannot be blank")
	private String currency = "INR";

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull(message = "Period cannot be null")
	private BudgetPeriod period;

	@Column(nullable = false)
	@NotNull(message = "Start date cannot be null")
	private LocalDate startDate;

	@Column(nullable = false)
	@NotNull(message = "End date cannot be null")
	private LocalDate endDate;

	@Column(nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal spentAmount = BigDecimal.ZERO;

	@Column(nullable = false)
	@NotNull(message = "Alert threshold cannot be null")
	@Min(value = 1, message = "Alert threshold must be at least 1")
	@Max(value = 100, message = "Alert threshold cannot be more than 100")
	private Integer alertThreshold; // Percentage, e.g., 80 for 80%

	@Column(nullable = false)
	@Builder.Default
	private Boolean isActive = true;
	
	// to avoid to manually sending "isActive": true and "spentAmount": 0 every time while creating a budget.
	// This tells Hibernate, "Right before you execute the SQL INSERT statement, run this method to double-check the data."
    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.spentAmount == null) {
            this.spentAmount = BigDecimal.ZERO;
        }
        if (this.currency == null) {
            this.currency = "INR";
        }
    }
}