package com.spendsmart.auth;

import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_Success() {
        User user = User.builder()
                .email("details@test.com")
                .passwordHash("encoded")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findByEmail("details@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("details@test.com");

        assertEquals("details@test.com", userDetails.getUsername());
        assertEquals("encoded", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@test.com"));
    }
}
