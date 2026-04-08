package com.spendsmart.auth.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

// OncePerRequestFilter guarantees this filter runs exactly once per HTTP request
// Without this guarantee, filters can run multiple times on forwarded requests
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Every authenticated request must include: Authorization: Bearer <token>
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            // Strip the "Bearer " prefix to get the raw JWT string
            String token = header.substring(7);

            if (jwtUtil.validateToken(token)) {
                // Token is valid — extract identity claims to build the Authentication object
                String email = jwtUtil.extractEmail(token);
                int userId = jwtUtil.extractUserId(token);

                // UsernamePasswordAuthenticationToken is Spring Security's standard auth object
                // Constructor: (principal, credentials, authorities)
                // principal = email (who they are), credentials = null (we don't need the password again)
                var auth = new UsernamePasswordAuthenticationToken(
                        email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

                // Store userId in the details field so controllers can retrieve it
                // without parsing the token again
                auth.setDetails(userId);

                // Placing the Authentication in SecurityContext tells Spring Security
                // "this request is authenticated" — all @PreAuthorize checks rely on this
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // If token is invalid we simply don't set authentication
            // Spring Security will then reject any protected endpoint automatically
        }

        // Always call chain.doFilter() to pass the request to the next filter/controller
        // Not calling this would block every single request
        chain.doFilter(request, response);
    }
}