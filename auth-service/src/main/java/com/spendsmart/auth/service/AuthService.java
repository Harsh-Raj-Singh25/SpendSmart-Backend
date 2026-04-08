package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.AuthResponse;
import com.spendsmart.auth.dto.ChangePasswordRequest;
import com.spendsmart.auth.dto.LoginRequest;
import com.spendsmart.auth.dto.RegisterRequest;
import com.spendsmart.auth.dto.UpdateProfileRequest;
import com.spendsmart.auth.entity.User;

// Service interface defines the contract — what operations are available
// The actual business logic lives in AuthServiceImpl
// This separation makes it easy to swap implementations (e.g., for testing)
public interface AuthService {

	// Creates a new user account — throws exception if email already exists
	User register(RegisterRequest request);

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
}