package com.spendsmart.auth;

import com.spendsmart.auth.dto.AuthResponse;
import com.spendsmart.auth.dto.LoginRequest;
import com.spendsmart.auth.dto.RegisterRequest;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import com.spendsmart.auth.service.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith initializes Mockito annotations (@Mock, @InjectMocks) before tests run
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // 1. Mock the dependencies - we don't want to hit the real DB or real bcrypt
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // 2. Inject the mocks into the actual service we are testing
    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    // Runs before every single test to set up fresh dummy data
    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1)
                .fullName("Test User")
                .email("test@test.com")
                .passwordHash("hashedPassword123")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    @Test
    void register_Success() {
        // Arrange: Setup our mock behaviors
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("test@test.com");
        request.setPassword("plainPassword");

        // When the service checks if email exists, return false
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        // When the service encodes the password, return a fake hash
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword123");
        // When the service saves the user, return our pre-built mockUser
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        // When the service generates a token, return a fake token
        when(jwtUtil.generateToken(anyString(), anyInt(), any(Role.class))).thenReturn("fake-jwt-token");

        // Act: Call the real method
        AuthResponse response = authService.register(request);

        // Assert: Verify the outcomes
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("test@test.com", response.getEmail());
        
        // Verify that the repository was actually called exactly once to save
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@test.com");

        // Simulate the database finding the email
        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        // Act & Assert: Check if the exact exception is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertEquals("Email already registered", exception.getMessage());
        
        // Verify that save() was NEVER called because it should fail fast
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword123")).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", 1, Role.USER)).thenReturn("fake-jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        // Simulate password mismatch
        when(passwordEncoder.matches("wrongPassword", "hashedPassword123")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }
}