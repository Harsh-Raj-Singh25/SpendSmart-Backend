package com.spendsmart.recurring.entity;

import com.spendsmart.recurring.model.enums.Frequency;
import com.spendsmart.recurring.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer recurringId;

	@Column(nullable = false)
	private Integer userId;

	@Column(nullable = false)
	private Integer categoryId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Frequency frequency;

	@Column(nullable = false)
	private LocalDate startDate;

	private LocalDate endDate; // Optional: When does this subscription end?

	@Column(nullable = false)
	private LocalDate nextDueDate;

	@Column(nullable = false)
	private Boolean isActive;

	private String description;

	@Column(nullable = false)
	private String paymentMethod;

	@PrePersist
	protected void onCreate() {
		if (this.isActive == null) {
			this.isActive = true; // Automatically active when created
		}
		if (this.nextDueDate == null) {
			this.nextDueDate = this.startDate; // First execution is usually the start date
		}
	}
}