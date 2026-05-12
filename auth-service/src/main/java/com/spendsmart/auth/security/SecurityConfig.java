package com.spendsmart.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// ============================================================================
// SECURITY CONFIGURATION — Defines which endpoints are public vs protected.
//
// HOW IT WORKS:
// 1. CSRF is disabled because we use stateless JWT (no cookies/sessions)
// 2. Session management is STATELESS — every request must carry its own JWT
// 3. permitAll() endpoints don't require a JWT — they're public
// 4. hasRole("ADMIN") endpoints require a JWT with role=ADMIN
// 5. authenticated() = everything else requires any valid JWT
// 6. JwtFilter runs BEFORE Spring's default filter to parse our JWT
// ============================================================================
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final CustomUserDetailsService userDetailsService;

	// ── SWAGGER WHITELIST ───────────────────────────────────────────────────
	// These paths must be completely public so the API Gateway can fetch
	// the OpenAPI documentation without providing a JWT.
	private static final String[] SWAGGER_WHITELIST = { "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
			"/auth/v3/api-docs/**" };

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF — not needed for stateless REST APIs
				.csrf(csrf -> csrf.disable())

				// STATELESS means Spring never creates or uses an HTTP session
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						// ── SWAGGER ENDPOINTS ───────────────────────────────
						.requestMatchers(SWAGGER_WHITELIST).permitAll()

						// ── PUBLIC ENDPOINTS — no JWT required ──────────────
						// Registration, login, Google OAuth, and password reset
						// must be accessible without authentication
						.requestMatchers("/auth/register", "/auth/login", "/auth/google", "/auth/forgot-password",
								"/auth/reset-password")
						.permitAll()

						// ── ADMIN-ONLY ENDPOINTS ────────────────────────────
						// Only users with role=ADMIN in their JWT can access
						.requestMatchers("/auth/admin/**").hasRole("ADMIN")

						// ── SUBSCRIPTION ENDPOINTS — accessible by other services ─
						// payment-service calls these to upgrade users after payment
						// expense/income services call to check subscription status
						.requestMatchers("/auth/subscription/**").permitAll()
						// Internal service-to-service endpoint used by notification-service
						.requestMatchers("/auth/internal/**").permitAll()
						
						// Actuator
						.requestMatchers("/actuator/**").permitAll()
						
						// ── EVERYTHING ELSE — requires valid JWT ────────────
						.anyRequest().authenticated())

				// Register our JwtFilter to run BEFORE Spring's default filter
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// BCrypt is a one-way hashing algorithm with a built-in salt
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}