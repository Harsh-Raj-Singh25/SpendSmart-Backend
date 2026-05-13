package com.spendsmart.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

	private static final List<String> PUBLIC_PATHS = List.of(
			"/auth/register",
			"/auth/login",
			"/auth/google",
			"/auth/forgot-password",
			"/auth/reset-password",
			"/v3/api-docs",
			"/swagger-ui",
			"/swagger-ui.html",
			"/swagger-resources",  // for swagger-ui resources : /swagger-resources/configuration/ui
			                      // /swagger-resources/configuration/security
			"/webjars",				// for swagger-ui webjars : /webjars/swagger-ui/3.52.5/swagger-ui.css
			"/actuator"
	);

	@Value("${jwt.secret:IN_ENV}")
	private String secret;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		HttpMethod method = exchange.getRequest().getMethod();

		if (method == HttpMethod.OPTIONS || isPublicPath(path)) {
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return unauthorized(exchange);
		}

		String token = authHeader.substring(7);
		Claims claims;
		try {
			claims = Jwts.parserBuilder()
					.setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
					.build()
					.parseClaimsJws(token)
					.getBody();
		} catch (JwtException | IllegalArgumentException e) {
			return unauthorized(exchange);
		}

		if (isAdminPath(path)) {
			String role = String.valueOf(claims.get("role"));
			if (!"ADMIN".equalsIgnoreCase(role)) {
				return forbidden(exchange);
			}
		}

		return chain.filter(exchange);
	}

	private boolean isPublicPath(String path) {
		if (path == null || path.isEmpty()) {
			return true;
		}
		// Check exact match or startswith with / suffix
		for (String openPath : PUBLIC_PATHS) {
			if (path.equals(openPath) || path.startsWith(openPath + "/")) {
				return true;
			}
		}
		// Also check if path contains API docs pattern (handles /service/v3/api-docs)c
		// This allows paths like /service/v3/api-docs, /service/swagger-ui, etc. to be public
		if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") 
				|| path.contains("/swagger-resources") || path.contains("/webjars")) {
			return true;
		}
		return false;
	}

	private boolean isAdminPath(String path) {
		return path.startsWith("/auth/admin")
				|| path.startsWith("/expenses/admin")
				|| path.startsWith("/incomes/admin")
				|| path.startsWith("/budgets/admin")
				|| path.startsWith("/categories/admin")
				|| path.startsWith("/recurring/admin")
				|| path.startsWith("/notifications/admin")
				|| path.startsWith("/analytics/admin");
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

	private Mono<Void> forbidden(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		return exchange.getResponse().setComplete();
	}

	@Override
	public int getOrder() {
		return -1;
	}
}
