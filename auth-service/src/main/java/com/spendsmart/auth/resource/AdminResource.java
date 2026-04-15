package com.spendsmart.auth.resource;

import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ============================================================================
// ADMIN CONTROLLER — All endpoints here are protected by hasRole("ADMIN")
// in SecurityConfig.java: .requestMatchers("/auth/admin/**").hasRole("ADMIN")
//
// HOW SPRING SECURITY PROTECTS THIS:
// 1. JwtFilter extracts the "role" claim from the JWT token
// 2. It creates a GrantedAuthority with "ROLE_ADMIN"
// 3. .hasRole("ADMIN") checks for "ROLE_ADMIN" in the authentication
// 4. If the user's role is USER (not ADMIN), they get 403 Forbidden
//
// Only users with role=ADMIN in the database can access these endpoints.
// ============================================================================
@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminResource {

	private final AuthService authService;

	// ── List All Users ──────────────────────────────────────────────────
	// Returns every user in the system (active + inactive).
	// The @JsonIgnore on passwordHash means passwords are never exposed.
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers() {
		log.info("ADMIN: Fetching all users");
		return ResponseEntity.ok(authService.getAllUsers());
	}

	// ── List Active Users Only ──────────────────────────────────────────
	// Filters to only show users with isActive = true.
	// Useful for the admin dashboard's "Active Users" panel.
	@GetMapping("/users/active")
	public ResponseEntity<List<User>> getActiveUsers() {
		log.info("ADMIN: Fetching active users");
		return ResponseEntity.ok(authService.getActiveUsers());
	}

	// ── User Count KPIs ─────────────────────────────────────────────────
	// Returns a JSON map like: { "total": 150, "active": 142 }
	// Used for the admin dashboard's headline KPI numbers.
	@GetMapping("/users/count")
	public ResponseEntity<Map<String, Long>> getUserCount() {
		log.info("ADMIN: Fetching user count");
		return ResponseEntity.ok(authService.getUserCount());
	}

	// ── Get Specific User ───────────────────────────────────────────────
	// Admin can view any user's profile by their ID.
	// Regular users can only view their own profile via /auth/profile/{id}.
	@GetMapping("/users/{userId}")
	public ResponseEntity<User> getUserById(@PathVariable int userId) {
		log.info("ADMIN: Fetching user with ID: {}", userId);
		return ResponseEntity.ok(authService.getUserById(userId));
	}

	// ── Suspend (Deactivate) User ───────────────────────────────────────
	// Sets isActive = false — the user can't login anymore.
	// Their data (expenses, income, budgets) is preserved intact.
	// This is a "soft delete" — reversible via the reactivate endpoint.
	@PutMapping("/users/{userId}/suspend")
	public ResponseEntity<Void> suspendUser(@PathVariable int userId) {
		log.info("ADMIN: Suspending user with ID: {}", userId);
		authService.suspendUser(userId);
		return ResponseEntity.ok().build();
	}

	// ── Reactivate User ─────────────────────────────────────────────────
	// Sets isActive = true — reverses a suspension.
	// The user can login again immediately with their existing credentials.
	@PutMapping("/users/{userId}/reactivate")
	public ResponseEntity<Void> reactivateUser(@PathVariable int userId) {
		log.info("ADMIN: Reactivating user with ID: {}", userId);
		authService.reactivateUser(userId);
		return ResponseEntity.ok().build();
	}

	// ── Permanently Delete User ─────────────────────────────────────────
	// HARD DELETE — removes the user record from the database permanently.
	// WARNING: This does NOT cascade-delete their expenses/income/budgets
	// in other services (those are in separate databases).
	// Use with caution — prefer suspend for most cases.
	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable int userId) {
		log.info("ADMIN: Permanently deleting user with ID: {}", userId);
		authService.deleteUser(userId);
		return ResponseEntity.ok().build();
	}
}
