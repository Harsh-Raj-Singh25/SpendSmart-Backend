package com.spendsmart.auth;

import com.spendsmart.auth.security.JwtFilter;
import com.spendsmart.auth.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_NoAuthHeader_DoesNotAuthenticate() throws Exception {
        JwtFilter filter = new JwtFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotAuthenticate() throws Exception {
        JwtFilter filter = new JwtFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtUtil.validateToken("bad-token")).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        JwtFilter filter = new JwtFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtUtil.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.extractEmail("good-token")).thenReturn("jwt@test.com");
        when(jwtUtil.extractUserId("good-token")).thenReturn(42);
        when(jwtUtil.extractRole("good-token")).thenReturn("USER");

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("jwt@test.com", authentication.getPrincipal());
        assertEquals(42, authentication.getDetails());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }
}
