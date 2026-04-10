package com.spendsmart.auth.resource;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthResource {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		log.info("POST /auth/register endpoint hit");
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

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
		// 1. Get the ID of the person making the request
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		// 2. Ensure they are only editing their own profile
		if (authenticatedUserId != userId) {
			throw new RuntimeException("Unauthorized: You cannot modify another user's profile.");
		}
		return ResponseEntity.ok(authService.updateProfile(userId, request));
	}

	@PutMapping("/password/{userId}")
	public ResponseEntity<Void> changePassword(@PathVariable int userId, @RequestBody ChangePasswordRequest request) {
		// 1. Get the ID of the person making the request
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		// 2. Ensure they are only editing their own profile
		if (authenticatedUserId != userId) {
			throw new RuntimeException("Unauthorized: You cannot modify another user's profile.");
		}
		authService.changePassword(userId, request);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/currency/{userId}")
	public ResponseEntity<Void> updateCurrency(@PathVariable int userId, @RequestParam String currency) {
		// 1. Get the ID of the person making the request
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		// 2. Ensure they are only editing their own profile
		if (authenticatedUserId != userId) {
			throw new RuntimeException("Unauthorized: You cannot modify another user's profile.");
		}
		authService.updateCurrency(userId, currency);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/deactivate/{userId}")
	public ResponseEntity<Void> deactivateAccount(@PathVariable int userId) {
		// 1. Get the ID of the person making the request
		int authenticatedUserId = (int) SecurityContextHolder.getContext().getAuthentication().getDetails();
		// 2. Ensure they are only editing their own profile
		if (authenticatedUserId != userId) {
			throw new RuntimeException("Unauthorized: You cannot modify another user's profile.");
		}
		authService.deactivateAccount(userId);
		return ResponseEntity.ok().build();
	}
}