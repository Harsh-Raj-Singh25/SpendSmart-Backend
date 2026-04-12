package com.spendsmart.budget.entity;

import com.spendsmart.budget.model.enums.BudgetPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
	private Integer userId;

	@Column(nullable = false)
	private Integer categoryId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal limitAmount;

	@Column(length = 3)
	@Builder.Default
	private String currency = "INR";

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BudgetPeriod period;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal spentAmount = BigDecimal.ZERO;

	@Column(nullable = false)
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