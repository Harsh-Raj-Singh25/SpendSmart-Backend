package com.spendsmart.auth.dto;

import lombok.Data;

// ============================================================================
// DTO for Google OAuth2 login.
// The Angular frontend uses Google Sign-In SDK to get an ID token,
// then sends it here for server-side verification.
//
// FLOW:
// 1. Frontend: User clicks "Sign in with Google"
// 2. Frontend: Google SDK returns an ID token (a JWT signed by Google)
// 3. Frontend: POST /auth/google { idToken: "eyJhb..." }
// 4. Backend: We verify this token using Google's API client library
// 5. Backend: Extract email, name from the verified token
// 6. Backend: Find or create the user, generate our own JWT, return AuthResponse
// ============================================================================
@Data
public class GoogleAuthRequest {
	// The ID token returned by Google Sign-In SDK on the frontend.
	// This is NOT an access token — it's a JWT containing the user's
	// email, name, and profile picture, signed by Google's servers.
	@jakarta.validation.constraints.NotBlank(message = "ID token is required")
	private String idToken;
}
