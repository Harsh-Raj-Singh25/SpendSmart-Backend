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
	private Integer userId;
	
	// this will link with category service
	@Column(nullable=false)
	private Long categoryId;
	
	@Column(nullable= false)
	private String title;
	//
	@Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
	
	@Column(length=3)
	@Builder.Default
	private String currency="INR";
	
	@Enumerated(EnumType.STRING)
	@Column
	private ExpenseType type;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;
	
	@Column(nullable = false)
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
