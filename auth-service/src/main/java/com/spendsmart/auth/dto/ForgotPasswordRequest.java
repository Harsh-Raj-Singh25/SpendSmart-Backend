package com.spendsmart.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// ============================================================================
// DTO for the "Forgot Password" step.
// User provides their email, and we send a 6-digit OTP to that email.
// The OTP is stored in the PasswordResetToken entity with a 10-minute expiry.
// ============================================================================
@Data
public class ForgotPasswordRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Please provide a valid email address")
	private String email;
}
