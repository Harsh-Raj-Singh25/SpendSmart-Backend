package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// @Service registers this class as a Spring-managed bean
// @RequiredArgsConstructor (Lombok) auto-generates a constructor for all final fields
// Spring uses that constructor to inject dependencies (constructor injection — best practice)
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder bean from SecurityConfig
	private final JwtUtil jwtUtil;

	@Override
	public User register(RegisterRequest request) {
		// Guard clause — fail fast before doing any DB work
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email already registered");
		}

		// Builder pattern creates User with all @Builder.Default values applied
		// We hash the password here — the raw password never touches the DB
		User user = User.builder().fullName(request.getFullName()).email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword())).build();

		// .save() performs INSERT since userId is not set yet
		return userRepository.save(user);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		// orElseThrow returns a clear error — we don't say "wrong password"
		// specifically to avoid leaking whether the email exists (security best
		// practice)
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("Invalid email or password"));

		// Reject login for deactivated accounts before checking password
		if (!user.isActive()) {
			throw new RuntimeException("Account is deactivated");
		}

		// passwordEncoder.matches() hashes the incoming raw password
		// and compares it to the stored BCrypt hash — they never need to be equal
		// strings
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new RuntimeException("Invalid email or password");
		}

		// Generate JWT containing email, userId, and role as claims
		String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole());

		return new AuthResponse(token, user.getUserId(), user.getFullName(), user.getEmail(), user.getRole());
	}

	@Override
	public void logout(String token) {
		// JWT is stateless — the server doesn't store sessions
		// Real logout happens on the client by discarding the token from storage
		// TODO Day 9: add Redis token blacklist for stricter invalidation
	}

	@Override
	public boolean validateToken(String token) {
		return jwtUtil.validateToken(token);
	}

	@Override
	public String refreshToken(String token) {
		// Extract the email from the existing (still valid) token
		// then issue a brand new token with a fresh expiry
		String email = jwtUtil.extractEmail(token);
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		return jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole());
	}

	@Override
	public User getUserById(int userId) {
		return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
	}

	@Override
	public User updateProfile(int userId, UpdateProfileRequest request) {
		// Fetch → modify → save is the standard JPA update pattern
		// We don't update email or password here — those have dedicated endpoints
		User user = getUserById(userId);
		user.setFullName(request.getFullName());
		user.setAvatarUrl(request.getAvatarUrl());
		user.setBio(request.getBio());
		user.setTimezone(request.getTimezone());

		// .save() on an existing entity (userId is set) performs UPDATE not INSERT
		return userRepository.save(user);
	}

	@Override
	public void changePassword(int userId, ChangePasswordRequest request) {
		User user = getUserById(userId);

		// Verify the current password first — prevents unauthorized password changes
		// even if someone intercepts a valid JWT token
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
			throw new RuntimeException("Current password is incorrect");
		}

		// Hash the new password before storing — never store raw passwords
		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}

	@Override
	public void updateCurrency(int userId, String currency) {
		User user = getUserById(userId);
		user.setCurrency(currency);
		userRepository.save(user);
	}

	@Override
	public void deactivateAccount(int userId) {
		User user = getUserById(userId);
		// Soft delete — isActive = false means the user can't login
		// but all their expenses, incomes, and budgets remain intact in the DB
		user.setActive(false);
		userRepository.save(user);
	}
}