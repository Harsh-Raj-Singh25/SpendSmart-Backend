package com.spendsmart.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ============================================================================
// DTO for the "Reset Password" step (after OTP verification).
// User provides their email, the 6-digit OTP received via email,
// and their new password.
//
// FLOW:
// 1. User received OTP via email (from the forgotPassword step)
// 2. User enters: email + OTP + new password
// 3. Backend verifies OTP is valid and not expired (10-minute window)
// 4. Backend hashes the new password and updates the user record
// 5. The OTP is marked as "used" so it can't be replayed
// ============================================================================
@Data
public class ResetPasswordRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Please provide a valid email address")
	private String email;

	// The 6-digit OTP from the email — validates as a non-blank string
	@NotBlank(message = "OTP is required")
	private String otp;

	@NotBlank(message = "New password is required")
	@Size(min = 8, message = "New password must be at least 8 characters")
	private String newPassword;
}
