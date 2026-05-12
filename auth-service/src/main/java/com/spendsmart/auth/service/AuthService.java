package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;

import java.util.List;
import java.util.Map;

// Service interface defines the contract — what operations are available
// The actual business logic lives in AuthServiceImpl
// This separation makes it easy to swap implementations (e.g., for testing)
public interface AuthService {

	// Creates a new user account — throws exception if email already exists
	// AuthResponse instead of User
	AuthResponse register(RegisterRequest request);

	// Validates credentials and returns a JWT token if successful
	AuthResponse login(LoginRequest request);

	// Stateless logout — client discards the token
	// In a future improvement, the token can be blacklisted in Redis
	void logout(String token);

	// Parses and validates the JWT signature and expiry
	boolean validateToken(String token);

	// Issues a fresh token using the email extracted from the old (valid) token
	String refreshToken(String token);

	// Fetches user by primary key — used by other services via REST
	User getUserById(int userId);

	// Fetches user by email — useful for Spring Security user loading
	User getUserByEmail(String email);

	// Updates non-sensitive profile fields (name, avatar, bio, timezone)
	User updateProfile(int userId, UpdateProfileRequest request);

	// Requires current password verification before allowing password change
	// Prevents unauthorized changes if someone gets a valid JWT
	void changePassword(int userId, ChangePasswordRequest request);

	// Updates just the display currency preference — affects dashboard rendering
	void updateCurrency(int userId, String currency);

	// Soft delete — sets isActive = false, preserving all historical data
	void deactivateAccount(int userId);

	// ── Admin Methods ──────────────────────────────────────────────────
	// These are used exclusively by AdminResource (protected by ADMIN role)

	// Returns ALL users in the system (active + inactive)
	List<User> getAllUsers();

	// Returns only users with isActive = true
	List<User> getActiveUsers();

	// Returns { "total": <count>, "active": <count> } for admin KPIs
	Map<String, Long> getUserCount();

	// Admin-only user creation (does not log in the user)
	User createUserByAdmin(AdminCreateUserRequest request);

	// Soft-deletes a user (sets isActive = false) — admin version of deactivateAccount
	void suspendUser(int userId);

	// Reverses a suspension — sets isActive = true
	void reactivateUser(int userId);

	// HARD deletes the user record from the database — irreversible
	void deleteUser(int userId);

	// Admin-only subscription controls
	void grantPremium(int userId);

	void revokePremium(int userId);

	// ── Google OAuth ───────────────────────────────────────────────────
	// Verifies a Google ID token and returns JWT (find-or-create user)
	AuthResponse googleLogin(GoogleAuthRequest request);

	// ── Forgot Password ────────────────────────────────────────────────
	// Generates a 6-digit OTP email for password reset
	void forgotPassword(ForgotPasswordRequest request);

	// Verifies OTP and resets password
	void resetPassword(ResetPasswordRequest request);

	// ── Subscription ───────────────────────────────────────────────────
	// Upgrades user to PREMIUM after successful payment
	void upgradeToPremium(int userId);

	// Returns user's current subscription status (FREE or PREMIUM)
	Map<String, Object> getSubscriptionStatus(int userId);
}