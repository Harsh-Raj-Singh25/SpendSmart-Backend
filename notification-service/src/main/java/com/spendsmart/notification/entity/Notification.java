package com.spendsmart.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer notificationId;

	@Column(nullable = false)
	private Integer recipientId; // Updated from userId to match diagram

	// Expected: BUDGET_ALERT, RECURRING_DUE, MONTHLY_SUMMARY, BUDGET_EXCEEDED,
	// SYSTEM
	@Column(nullable = false, length = 50)
	private String type;

	// Expected: INFO, WARNING, CRITICAL
	@Column(nullable = false, length = 20)
	private String severity;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 1000)
	private String message;

	// Optional fields to link the notification to a specific record (e.g., budgetId
	// = 4)
	private Integer relatedId;
	private String relatedType;

	@Column(nullable = false)
	@Builder.Default
	private boolean isRead = false;

	@Column(nullable = false)
	@Builder.Default
	private boolean isAcknowledged = false; // Requires explicit user confirmation

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
}