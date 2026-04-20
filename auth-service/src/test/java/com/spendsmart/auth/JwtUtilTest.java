package com.spendsmart.auth;

import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil buildUtil(long expirationMillis) {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "expiration", expirationMillis);
        return jwtUtil;
    }

    @Test
    void generateAndExtractClaims_Success() {
        JwtUtil jwtUtil = buildUtil(60_000L);

        String token = jwtUtil.generateToken("jwt@test.com", 7, Role.ADMIN);

        assertNotNull(token);
        assertEquals("jwt@test.com", jwtUtil.extractEmail(token));
        assertEquals(7, jwtUtil.extractUserId(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        JwtUtil jwtUtil = buildUtil(60_000L);

        assertFalse(jwtUtil.validateToken("not-a-jwt"));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        JwtUtil jwtUtil = buildUtil(-1000L);
        String token = jwtUtil.generateToken("jwt@test.com", 9, Role.USER);

        assertFalse(jwtUtil.validateToken(token));
    }
}
