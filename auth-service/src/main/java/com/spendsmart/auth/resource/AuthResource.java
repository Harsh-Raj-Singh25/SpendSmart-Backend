package com.spendsmart.auth.resource;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.exception.BadRequestException;
import com.spendsmart.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// ============================================================================
// AUTH CONTROLLER — Public and authenticated endpoints for user auth + profile.
//
// ENDPOINT SECURITY (defined in SecurityConfig.java):
// - permitAll: /auth/register, /auth/login, /auth/google,
//              /auth/forgot-password, /auth/reset-password
// - hasRole("ADMIN"): /auth/admin/**  (handled by AdminResource)
// - authenticated: everything else (requires valid JWT)
// ============================================================================
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthResource {

	private final AuthService authService;

	// ── PUBLIC ENDPOINTS (no JWT required) ──────────────────────────────

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		log.info("POST /auth/register endpoint hit");
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	// Google OAuth2 login — the frontend sends the Google ID token here
	// for server-side verification. No password needed.
	@PostMapping("/google")
	public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {
		log.info("POST /auth/google endpoint hit");
		return ResponseEntity.ok(authService.googleLogin(request));
	}

	// Step 1 of password reset — sends a 6-digit OTP to the user's email
	@PostMapping("/forgot-password")
	public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ResponseEntity.ok(Map.of("message", "OTP sent to your email. Valid for 10 minutes."));
	}

	// Step 2 of password reset — verifies OTP and sets new password
	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(Map.of("message", "Password reset successful. You can now login with your new password."));
	}

	// ── AUTHENTICATED ENDPOINTS (JWT required) ─────────────────────────

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
		authService.logout(header.substring(7));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<Map<String, String>> refresh(@RequestHeader("Authorization") String header) {
		String newToken = authService.refreshToken(header.substring(7));
		return ResponseEntity.ok(Map.of("token", newToken));
	}

	@GetMapping("/profile/{userId}")
	public ResponseEntity<User> getProfile(@PathVariable int userId) {
		return ResponseEntity.ok(authService.getUserById(userId));
	}

	@PutMapping("/profile/{userId}")
	public ResponseEntity<User> updateProfile(@PathVariable int userId, @RequestBody UpdateProfileRequest request) {
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (authenticatedUserId != userId) {
			throw new BadRequestException("Unauthorized: You cannot modify another user's profile.");
		}
		return ResponseEntity.ok(authService.updateProfile(userId, request));
	}

	@PutMapping("/password/{userId}")
	public ResponseEntity<Void> changePassword(@PathVariable int userId, @Valid @RequestBody ChangePasswordRequest request) {
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (authenticatedUserId != userId) {
			throw new BadRequestException("Unauthorized: You cannot modify another user's profile.");
		}
		authService.changePassword(userId, request);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/currency/{userId}")
	public ResponseEntity<Void> updateCurrency(@PathVariable int userId, @RequestParam String currency) {
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (authenticatedUserId != userId) {
			throw new BadRequestException("Unauthorized: You cannot modify another user's profile.");
		}
		authService.updateCurrency(userId, currency);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/deactivate/{userId}")
	public ResponseEntity<Void> deactivateAccount(@PathVariable int userId) {
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (authenticatedUserId != userId) {
			throw new BadRequestException("Unauthorized: You cannot modify another user's profile.");
		}
		authService.deactivateAccount(userId);
		return ResponseEntity.ok().build();
	}

	// ── SUBSCRIPTION ENDPOINTS (called by payment-service) ─────────────

	// Called by payment-service after successful Razorpay payment
	@PutMapping("/subscription/{userId}/upgrade")
	public ResponseEntity<Void> upgradeToPremium(@PathVariable int userId) {
		authService.upgradeToPremium(userId);
		return ResponseEntity.ok().build();
	}

	// Called by expense-service and income-service to check if user can add more transactions
	@GetMapping("/subscription/{userId}")
	public ResponseEntity<Map<String, Object>> getSubscriptionStatus(@PathVariable int userId) {
		return ResponseEntity.ok(authService.getSubscriptionStatus(userId));
	}

	// Internal endpoint for trusted inter-service communication.
	// Called by notification-service to fetch recipient email for critical alerts.
	@GetMapping("/internal/users/{userId}/email")
	public ResponseEntity<Map<String, String>> getUserEmail(@PathVariable int userId) {
		User user = authService.getUserById(userId);
		return ResponseEntity.ok(Map.of("email", user.getEmail()));
	}
}