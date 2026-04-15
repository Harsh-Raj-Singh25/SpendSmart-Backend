package com.spendsmart.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.model.enums.SubscriptionType;

// @Entity tells Spring Data JPA that this class maps to a database table
// @Table(name = "users") specifies the exact table name in MySQL
@Entity
@Table(name = "users")
// Lombok annotations to auto-generate getters, setters, constructors, and builder pattern
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	// @Id marks this as the primary key
	// IDENTITY strategy means MySQL auto-increments this value
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userId;

	// @Column(nullable = false) adds a NOT NULL constraint at DB level
	@Column(nullable = false)
	private String fullName;

	// unique = true ensures no two users can share the same email
	@Column(nullable = false, unique = true)
	private String email;

	// We never store raw passwords — only the BCrypt hash goes here.
	// Nullable because Google OAuth users don't have a password.
	@JsonIgnore
	private String passwordHash;

	// @Builder.Default sets the default value when using the builder pattern
	// Without this, builder-created objects would have null for these fields
	@Builder.Default
	private String currency = "INR";

	@Builder.Default
	private String timezone = "Asia/Kolkata";

	private String avatarUrl;
	private String bio;

	// provider tracks how the user signed up — LOCAL = email/password, GOOGLE =
	// OAuth2
	@Builder.Default
	private String provider = "LOCAL";

	// Soft delete flag — we never hard delete users, just set isActive = false
	// This preserves historical financial data even for deactivated accounts
	@Builder.Default
	private boolean isActive = true;

	// Auto-set at DB insert time — Hibernate manages this automatically
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	// Used by Analytics-Service to compute the Financial Health Score
	private double monthlyBudget;

	// Role-based access control — USER gets personal finance access, ADMIN gets
	// platform access
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Role role = Role.USER;

	// ── Subscription / Freemium Fields ───────────────────────────────────
	// FREE = 7 transactions/day, PREMIUM = unlimited
	// After Razorpay payment, subscriptionType is set to PREMIUM
	// and premiumExpiresAt is set to 30 days from the payment date.
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private SubscriptionType subscriptionType = SubscriptionType.FREE;

	// null for FREE users; set to payment_date + 30 days for PREMIUM users.
	// When this date passes, the user should be treated as FREE again.
	private LocalDateTime premiumExpiresAt;
}