package com.spendsmart.expense.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.spendsmart.expense.model.enums.ExpenseType;
import com.spendsmart.expense.model.enums.PaymentMethod;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name="expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense { 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long expenseId;
	
	//this links the expense to the user in the auth database
	@Column(nullable=false)
	@NotNull(message = "User ID cannot be null")
	private Integer userId;
	
	// this will link with category service
	@Column(nullable=false)
	@NotNull(message = "Category ID cannot be null")
	private Integer categoryId;
	
	@Column(nullable= false)
	@NotBlank(message = "Title cannot be blank")
	private String title;
	//
	@Column(nullable = false, precision = 10, scale = 2)
	@NotNull(message = "Amount cannot be null")
	@Positive(message = "Amount must be positive")
    private BigDecimal amount;
	
	@Column(length=3)
	@Builder.Default
	@NotBlank(message = "Currency cannot be blank")
	private String currency="INR";
	
	@Enumerated(EnumType.STRING)
	@Column
	@NotNull(message = "Expense type cannot be null")
	private ExpenseType type;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull(message = "Payment method cannot be null")
	private PaymentMethod paymentMethod;
	
	@Column(nullable = false)
	@NotNull(message = "Date cannot be null")
	private LocalDate date;
	
	@Column(length=500)
	private String notes;
	
	private String receiptUrl;
	
	@Column(nullable = false)
	private Boolean isRecurring;
	
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
}
