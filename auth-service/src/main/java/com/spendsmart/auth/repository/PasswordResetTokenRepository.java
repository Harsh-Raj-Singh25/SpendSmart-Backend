package com.spendsmart.auth.repository;

import com.spendsmart.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// ============================================================================
// Repository for password reset OTP tokens.
// Spring Data JPA auto-generates SQL from these method names.
// ============================================================================
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	// Finds the most recent OTP for a given email that hasn't been used yet.
	// We use "OrderByCreatedAtDesc" to get the latest one first, in case
	// the user requested multiple OTPs (only the latest should be valid).
	Optional<PasswordResetToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}
