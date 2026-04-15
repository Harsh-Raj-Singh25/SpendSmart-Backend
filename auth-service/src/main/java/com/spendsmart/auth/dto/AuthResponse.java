package com.spendsmart.auth.dto;

import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.model.enums.SubscriptionType;

import lombok.AllArgsConstructor;
import lombok.Data;

// ============================================================================
// AUTH RESPONSE — Returned after successful registration, login, or Google auth.
// This is what the Angular frontend receives and stores (typically in localStorage).
//
// The token is a JWT that must be sent as "Authorization: Bearer <token>"
// header on every subsequent API request.
// ============================================================================
@Data
@AllArgsConstructor
public class AuthResponse {
	private String token;           // JWT token for subsequent API calls
	private int userId;             // User's unique ID across all services
	private String fullName;        // Display name for the UI
	private String email;           // User's email address
	private Role role;              // USER or ADMIN — controls UI view
	private SubscriptionType subscriptionType; // FREE or PREMIUM — controls transaction limits
}