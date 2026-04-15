package com.spendsmart.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

// ============================================================================
// PASSWORD RESET TOKEN — Stores OTPs for the "Forgot Password" flow.
//
// LIFECYCLE:
// 1. User hits POST /auth/forgot-password with their email
// 2. We generate a random 6-digit OTP (e.g., "483921")
// 3. We save it here with a 10-minute expiry (expiresAt = now + 10 min)
// 4. We send the OTP to the user's email via notification-service
// 5. User hits POST /auth/reset-password with email + OTP + newPassword
// 6. We find this token, verify it's not expired and not already used
// 7. We hash the new password, save it to User entity, mark this token as used
//
// SECURITY NOTES:
// - OTP is 6 digits (1 million combinations) — sufficient for email-verified resets
// - 10-minute expiry prevents brute-force attacks
// - "used" flag prevents replay attacks (same OTP used twice)
// - Old unused tokens are not deleted — they just expire naturally
// ============================================================================
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// The email address this OTP was generated for
	@Column(nullable = false)
	private String email;

	// The 6-digit OTP string (e.g., "483921")
	@Column(nullable = false)
	private String otp;

	// When this OTP expires — set to 10 minutes from creation
	@Column(nullable = false)
	private LocalDateTime expiresAt;

	// Whether this OTP has already been used for a password reset
	@Builder.Default
	private boolean used = false;

	// When this token was created — for audit purposes
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
}
