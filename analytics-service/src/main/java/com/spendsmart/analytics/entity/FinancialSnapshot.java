package com.spendsmart.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer snapshotId;

	@Column(nullable = false)
	private Integer userId;

	@Column(nullable = false, length = 20)
	private String period; // e.g., "MONTHLY"

	@Column(nullable = false)
	private Integer year;

	@Column(nullable = false)
	private Integer month;

	@Column(precision = 12, scale = 2)
	private BigDecimal totalIncome;

	@Column(precision = 12, scale = 2)
	private BigDecimal totalExpenses;

	@Column(precision = 12, scale = 2)
	private BigDecimal netSavings;

	@Column(precision = 5, scale = 2)
	private BigDecimal savingsRate; // Percentage

	@Column(length = 100)
	private String topCategory;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
}