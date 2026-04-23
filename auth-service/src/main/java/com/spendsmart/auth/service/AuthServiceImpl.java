package com.spendsmart.auth.service;

import com.spendsmart.auth.client.NotificationClient;
import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.PasswordResetToken;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.exception.BadRequestException;
import com.spendsmart.auth.exception.ResourceNotFoundException;
import com.spendsmart.auth.model.enums.SubscriptionType;
import com.spendsmart.auth.repository.PasswordResetTokenRepository;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// ============================================================================
// AUTH SERVICE IMPLEMENTATION — The main business logic class for authentication.
//
// This class handles:
// 1. Registration / Login (email+password and Google OAuth)
// 2. JWT token management (generate, validate, refresh)
// 3. Profile management (update, change password, deactivate)
// 4. Admin operations (list/suspend/reactivate/delete users)
// 5. Forgot Password flow (OTP generation + verification)
// 6. Subscription management (upgrade to PREMIUM after payment)
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder bean from SecurityConfig
	private final JwtUtil jwtUtil;
	private final PasswordResetTokenRepository resetTokenRepository;
	private final NotificationClient notificationClient;

	// Google OAuth2 client ID — set in application.yml
	// This is used to verify that the Google ID token was issued for OUR app
	@Value("${google.client-id:PLACEHOLDER_GOOGLE_CLIENT_ID}")
	private String googleClientId;
	
	String serviceProvider="GOOGLE"; 
	// REGISTRATION — Creates a new user account with email + password
	@Override
	public AuthResponse register(RegisterRequest request) {
		log.info("Attempting to register new user with email: {}", request.getEmail());

		// Guard clause — fail fast before doing any DB work
		if (userRepository.existsByEmail(request.getEmail())) {
			log.warn("Registration failed: Email {} is already registered", request.getEmail());
			throw new BadRequestException("Email already registered");
		}

		// Builder pattern creates User with all @Builder.Default values applied
		User user = User.builder().fullName(request.getFullName()).email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword())).build();

		// Save the user to the database to generate the userId
		User savedUser = userRepository.save(user);
		log.info("User registered successfully with ID: {}", savedUser.getUserId());

		// Generate a JWT token immediately so the user is logged in upon registration
		String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getUserId(), savedUser.getRole());

		// Return the secure DTO, completely hiding the password hash and DB metadata
		return new AuthResponse(token, savedUser.getUserId(), savedUser.getFullName(), savedUser.getEmail(),
				savedUser.getRole(), savedUser.getSubscriptionType());
	}
 
	// LOGIN — Validates email + password credentials and returns JWT token
	@Override
	public AuthResponse login(LoginRequest request) {
		log.info("Attempting login for email: {}", request.getEmail());

		// orElseThrow returns a clear error — we don't say "wrong password"
		// specifically to avoid leaking whether the email exists (security best practice)
		User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
			log.warn("Login failed: User not found for email: {}", request.getEmail());
			return new BadRequestException("Invalid email or password");
		});

		// Reject login for deactivated accounts before checking password
		if (!user.isActive()) {
			throw new ResourceNotFoundException("Account is deactivated");
		}

		// Google OAuth users don't have a password — they must use Google login
		if (serviceProvider.equals(user.getProvider()) && user.getPasswordHash() == null) {
			throw new BadRequestException("This account uses Google Sign-In. Please login with Google.");
		}

		// passwordEncoder.matches() hashes the incoming raw password
		// and compares it to the stored BCrypt hash
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new BadRequestException("Invalid email or password");
		}

		// Auto-expire premium subscription if past the expiry date
		checkAndExpirePremium(user);

		// Generate JWT containing email, userId, and role as claims
		String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole());
		log.info("Login successful for user ID: {}", user.getUserId());
		return new AuthResponse(token, user.getUserId(), user.getFullName(), user.getEmail(),
				user.getRole(), user.getSubscriptionType());
	}
 
	// GOOGLE OAUTH2 LOGIN — Verifies Google ID token, find-or-create user 
	@Override
	public AuthResponse googleLogin(GoogleAuthRequest request) {
		log.info("Attempting Google OAuth login");

		try {
			// Step 1: Create a verifier that checks the token's signature and audience
			// The audience must match our Google Client ID to prevent token theft
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
					new NetHttpTransport(), GsonFactory.getDefaultInstance())
					.setAudience(Collections.singletonList(googleClientId))
					.build();

			// Step 2: Verify the token — this checks:
			// - Token signature (signed by Google's private key)
			// - Token expiry (not expired)
			// - Audience (issued for our app, not someone else's)
			GoogleIdToken idToken = verifier.verify(request.getIdToken());
			if (idToken == null) {
				throw new BadRequestException("Invalid Google ID token");
			}

			// Step 3: Extract user info from the verified token payload
			GoogleIdToken.Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String fullName = (String) payload.get("name");
			String avatarUrl = (String) payload.get("picture");

			// Step 4: Find existing user or create a new one
			// If the user already exists (registered before with Google or email),
			// we just log them in. If not, we create a new account automatically.
			Optional<User> existingUser = userRepository.findByEmail(email);
			User user;

			if (existingUser.isPresent()) {
				// User already exists — just update their Google profile info
				user = existingUser.get();
				if (!user.isActive()) {
					throw new ResourceNotFoundException("Account is deactivated");
				}
			} else {
				// New user — create account with Google provider
				// No password hash needed — Google handles authentication
				user = User.builder()
						.fullName(fullName != null ? fullName : "Google User")
						.email(email)
						.provider(serviceProvider)
						.avatarUrl(avatarUrl)
						.build();
				user = userRepository.save(user);
				log.info("New Google user created with ID: {}", user.getUserId());
			}

			// Auto-expire premium subscription if past the expiry date
			checkAndExpirePremium(user);

			// Step 5: Generate our own JWT for the user
			String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole());
			return new AuthResponse(token, user.getUserId(), user.getFullName(), user.getEmail(),
					user.getRole(), user.getSubscriptionType());

		} catch (BadRequestException | ResourceNotFoundException e) {
			throw e; // Re-throw our own exceptions
		} catch (Exception e) {
			log.error("Google login failed: {}", e.getMessage());
			throw new BadRequestException("Google authentication failed: " + e.getMessage());
		}
	}

	// FORGOT PASSWORD — Generates OTP and sends it via email 
	private Random random=new Random();
	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		log.info("Forgot password request for email: {}", request.getEmail());

		// Step 1: Verify the email exists in our system
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail()));

		// Google OAuth users can't reset passwords — they don't have one
		if ("GOOGLE".equals(user.getProvider()) && user.getPasswordHash() == null) {
			throw new BadRequestException("This account uses Google Sign-In. Password reset is not available.");
		}

		// Step 2: Generate a random 6-digit OTP
		// Random().nextInt(900000) generates 0-899999, + 100000 gives 100000-999999
		String otp = String.valueOf(100000 + random.nextInt(900000));

		// Step 3: Save the OTP with a 10-minute expiry window
		PasswordResetToken token = PasswordResetToken.builder()
				.email(request.getEmail())
				.otp(otp)
				.expiresAt(LocalDateTime.now().plusMinutes(10))
				.build();
		resetTokenRepository.save(token);

		// Step 4: Send the OTP via email using the notification-service
		// The notification-service handles actual SMTP delivery
		String subject = "SpendSmart — Password Reset OTP";
		String body = "Hi " + user.getFullName() + ",\n\n"
				+ "Your password reset OTP is: " + otp + "\n\n"
				+ "This OTP is valid for 10 minutes.\n"
				+ "If you didn't request this, please ignore this email.\n\n"
				+ "— SpendSmart Team";

		try {
			notificationClient.sendEmail(request.getEmail(), subject, body);
			log.info("OTP email sent successfully to: {}", request.getEmail());
		} catch (Exception e) {
			log.error("Failed to send OTP email: {}", e.getMessage());
			// We don't throw here — the OTP is saved in DB, user can retry
		}
	}
 
	// RESET PASSWORD — Verifies OTP and sets new password 
	@Override
	public void resetPassword(ResetPasswordRequest request) {
		log.info("Password reset attempt for email: {}", request.getEmail());

		// Step 1: Find the latest unused OTP for this email
		PasswordResetToken token = resetTokenRepository
				.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
				.orElseThrow(() -> new BadRequestException("No valid OTP found. Please request a new one."));

		// Step 2: Check if the OTP has expired (10-minute window)
		if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BadRequestException("This OTP has expired. Please request a new one.");
		}

		// Step 3: Verify the OTP matches
		if (!token.getOtp().equals(request.getOtp())) {
			throw new BadRequestException("Invalid OTP. Please check and try again.");
		}

		// Step 4: Mark the OTP as used (prevents replay attacks)
		token.setUsed(true);
		resetTokenRepository.save(token);

		// Step 5: Update the user's password
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		log.info("Password reset successful for email: {}", request.getEmail());
	}

	// TOKEN MANAGEMENT
	@Override
	public void logout(String token) {
		// JWT is stateless — the server doesn't store sessions
		// Real logout happens on the client by discarding the token from storage 
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
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		return jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole());
	}

	// PROFILE MANAGEMENT
	@Override
	public User getUserById(int userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
	}

	@Override
	public User updateProfile(int userId, UpdateProfileRequest request) {
		// Fetch → modify → save is the standard JPA update pattern
		User user = getUserById(userId);
		user.setFullName(request.getFullName());
		user.setAvatarUrl(request.getAvatarUrl());
		user.setBio(request.getBio());
		user.setTimezone(request.getTimezone());
		return userRepository.save(user);
	}

	@Override
	public void changePassword(int userId, ChangePasswordRequest request) {
		User user = getUserById(userId);

		// Verify the current password first — prevents unauthorized password changes
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
			throw new BadRequestException("Current password is incorrect");
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
		user.setActive(false);
		userRepository.save(user);
	}

	// ADMIN OPERATIONS — Only accessible by users with ADMIN role
	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public List<User> getActiveUsers() {
		return userRepository.findByIsActive(true);
	}

	@Override
	public Map<String, Long> getUserCount() {
		Map<String, Long> counts = new HashMap<>();// Returns a map like: { "total": 150, "active": 142 }
		counts.put("total", userRepository.count());
		counts.put("active", userRepository.countByIsActive(true));
		return counts;
	}

	@Override
	public void suspendUser(int userId) {
		User user = getUserById(userId);
		user.setActive(false);
		userRepository.save(user);
		log.info("User {} suspended by admin", userId);
	}

	@Override
	public void reactivateUser(int userId) {
		User user = getUserById(userId);
		user.setActive(true);
		userRepository.save(user);
		log.info("User {} reactivated by admin", userId);
	}

	@Override
	public void deleteUser(int userId) {
		// Hard delete — permanently removes the user from the database
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User not found with ID: " + userId);
		}
		userRepository.deleteById(userId);
		log.info("User {} permanently deleted by admin", userId);
	}

	// SUBSCRIPTION MANAGEMENT — Freemium model

	@Override
	public void upgradeToPremium(int userId) {
		// Called by payment-service after successful Razorpay payment verification
		User user = getUserById(userId);
		user.setSubscriptionType(SubscriptionType.PREMIUM);
		// Premium lasts 30 days from now
		user.setPremiumExpiresAt(LocalDateTime.now().plusDays(30));
		userRepository.save(user);
		log.info("User {} upgraded to PREMIUM until {}", userId, user.getPremiumExpiresAt());
	}

	@Override
	public Map<String, Object> getSubscriptionStatus(int userId) {
		// Returns subscription info for the user — used by expense/income services
		// to check if the user can add more transactions today
		User user = getUserById(userId);
		checkAndExpirePremium(user);

		Map<String, Object> status = new HashMap<>();
		status.put("subscriptionType", user.getSubscriptionType());
		status.put("premiumExpiresAt", user.getPremiumExpiresAt());
		return status;
	}

	// PRIVATE HELPERS

	// Checks if a PREMIUM subscription has expired and auto-downgrades to FREE
	private void checkAndExpirePremium(User user) {
		if (user.getSubscriptionType() == SubscriptionType.PREMIUM
				&& user.getPremiumExpiresAt() != null
				&& user.getPremiumExpiresAt().isBefore(LocalDateTime.now())) {
			log.info("Premium subscription expired for user {}. Downgrading to FREE.", user.getUserId());
			user.setSubscriptionType(SubscriptionType.FREE);
			user.setPremiumExpiresAt(null);
			userRepository.save(user);
		}
	}
}