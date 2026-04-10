package com.spendsmart.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.spendsmart.auth.model.enums.Role;

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

	// We never store raw passwords — only the BCrypt hash goes here
	@Column(nullable = false)
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

	// Auto-set at object creation time — we don't expect the caller to pass this
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	// Used by Analytics-Service to compute the Financial Health Score
	private double monthlyBudget;

	// Role-based access control — USER gets personal finance access, ADMIN gets
	// platform access
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Role role = Role.USER;
}