package com.spendsmart.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.spendsmart.auth.model.enums.Role;

import java.security.Key;
import java.util.Date;

// @Component registers this as a Spring bean so it can be injected anywhere
@Component
public class JwtUtil {

	// @Value injects values from application.properties at runtime
	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration; // in milliseconds — 86400000 = 24 hours

	// Convert the secret string into a cryptographic signing key
	// Keys.hmacShaKeyFor() ensures the key is long enough for HS256
	private Key getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

//	public String generateToken(String email, int userId, String role) {
//		return Jwts.builder().setSubject(email) // standard "sub" claim — identifies the user
//				.claim("userId", userId) // custom claim — avoids a DB lookup later
//				.claim("role", role) // custom claim — used for access control
//				.setIssuedAt(new Date()) // "iat" claim — when the token was issued
//				.setExpiration(new Date(System.currentTimeMillis() + expiration)) // "exp" claim
//				.signWith(getKey(), SignatureAlgorithm.HS256) // sign with HMAC-SHA256
//				.compact(); // serialise to the final JWT string
//	}
	public String generateToken(String email, int userId, Role role) {
		return Jwts.builder().subject(email) // subject() instead of setSubject()
				.claim("userId", userId).claim("role", role).issuedAt(new Date()) // issuedAt() instead of setIssuedAt()
				.expiration(new Date(System.currentTimeMillis() + expiration)) // expiration()
				.signWith(getKey()) // Algorithm is inferred from the key automatically
				.compact();
	}

	// Extract the email (stored as "sub") from the token
	public String extractEmail(String token) {
		return getClaims(token).getSubject();
	}

	// Extract our custom userId claim — cast to int since JSON numbers parse as
	// Integer
	public int extractUserId(String token) {
		return (int) getClaims(token).get("userId");
	}

	public boolean validateToken(String token) {
		try {
			// parseClaimsJws() both parses and verifies the signature
			// It throws an exception for any invalid, expired, or tampered token
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			// JwtException covers expired, malformed, unsupported, and signature errors
			return false;
		}
	}

	// Private helper — parses the token and returns all claims
	// Throws automatically if the token is invalid or expired
//	@SuppressWarnings("deprecation")
//	private Claims getClaims(String token) {
//	// return
//	// Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).
//	// getBody();
//	 return Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
//	}
	private Claims getClaims(String token) {
		return Jwts.parser().setSigningKey(getKey()).build().parseSignedClaims(token).getPayload();
	}
	
	public String extractRole(String token) {
	    return (String) getClaims(token).get("role");
	}

}