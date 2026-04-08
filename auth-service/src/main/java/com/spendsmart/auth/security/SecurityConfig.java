package com.spendsmart.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF — not needed for stateless REST APIs
				// CSRF protection is only relevant for session-based (cookie) auth
				.csrf(csrf -> csrf.disable())

				// STATELESS means Spring never creates or uses an HTTP session
				// Every request must carry its own JWT — no "remember me" session
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						// These three endpoints must be public — user isn't logged in yet
						.requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
						// Every other endpoint requires a valid JWT in the Authorization header
						.anyRequest().authenticated())

				// Register our JwtFilter to run BEFORE Spring's default username/password
				// filter
				// This way Spring Security sees our authentication before trying its own
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// BCrypt is a one-way hashing algorithm with a built-in salt
		// The same password hashed twice produces different hashes — prevents rainbow
		// table attacks
		// Default strength factor is 10 (2^10 = 1024 hashing rounds) — secure and fast
		// enough
		return new BCryptPasswordEncoder();
	}
}