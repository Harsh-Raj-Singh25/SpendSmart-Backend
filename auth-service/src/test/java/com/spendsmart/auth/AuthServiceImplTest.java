package com.spendsmart.auth;

import com.spendsmart.auth.client.NotificationClient;
import com.spendsmart.auth.dto.AuthResponse;
import com.spendsmart.auth.dto.ChangePasswordRequest;
import com.spendsmart.auth.dto.ForgotPasswordRequest;
import com.spendsmart.auth.dto.GoogleAuthRequest;
import com.spendsmart.auth.dto.LoginRequest;
import com.spendsmart.auth.dto.RegisterRequest;
import com.spendsmart.auth.dto.ResetPasswordRequest;
import com.spendsmart.auth.dto.UpdateProfileRequest;
import com.spendsmart.auth.entity.PasswordResetToken;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.exception.BadRequestException;
import com.spendsmart.auth.exception.ResourceNotFoundException;
import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.model.enums.SubscriptionType;
import com.spendsmart.auth.repository.PasswordResetTokenRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private NotificationClient notificationClient;

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

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@test.com");
        request.setPassword("secret");

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_DeactivatedUser_ThrowsResourceNotFound() {
        mockUser.setActive(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authService.login(request));
        assertEquals("Account is deactivated", exception.getMessage());
    }

    @Test
    void login_GoogleOnlyUser_ThrowsBadRequest() {
        mockUser.setProvider("GOOGLE");
        mockUser.setPasswordHash(null);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("anything");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertTrue(exception.getMessage().contains("Google Sign-In"));
    }

    @Test
    void login_ExpiredPremium_DowngradesBeforeReturning() {
        mockUser.setSubscriptionType(SubscriptionType.PREMIUM);
        mockUser.setPremiumExpiresAt(LocalDateTime.now().minusDays(1));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword123")).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", 1, Role.USER)).thenReturn("fresh-token");

        AuthResponse response = authService.login(request);

        assertEquals("fresh-token", response.getToken());
        assertEquals(SubscriptionType.FREE, mockUser.getSubscriptionType());
        assertNull(mockUser.getPremiumExpiresAt());
        verify(userRepository, atLeastOnce()).save(mockUser);
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("absent@test.com");

        when(userRepository.findByEmail("absent@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(request));
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPassword_GoogleUser_ThrowsException() {
        mockUser.setProvider("GOOGLE");
        mockUser.setPasswordHash(null);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(BadRequestException.class, () -> authService.forgotPassword(request));
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPassword_EmailDispatchFailure_DoesNotThrow() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        doThrow(new RuntimeException("smtp down")).when(notificationClient)
                .sendEmail(eq("test@test.com"), anyString(), anyString());

        assertDoesNotThrow(() -> authService.forgotPassword(request));
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_NoToken_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setOtp("123456");
        request.setNewPassword("new-pass");

        when(resetTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setOtp("123456");
        request.setNewPassword("new-pass");

        PasswordResetToken token = PasswordResetToken.builder()
                .email("test@test.com")
                .otp("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(resetTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("test@test.com"))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_InvalidOtp_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setOtp("999999");
        request.setNewPassword("new-pass");

        PasswordResetToken token = PasswordResetToken.builder()
                .email("test@test.com")
                .otp("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(resetTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("test@test.com"))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_Success_UpdatesPasswordAndMarksTokenUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setOtp("123456");
        request.setNewPassword("new-pass");

        PasswordResetToken token = PasswordResetToken.builder()
                .email("test@test.com")
                .otp("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(resetTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("test@test.com"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        assertDoesNotThrow(() -> authService.resetPassword(request));
        assertTrue(token.isUsed());
        assertEquals("new-hash", mockUser.getPasswordHash());
        verify(resetTokenRepository, atLeastOnce()).save(token);
        verify(userRepository, atLeastOnce()).save(mockUser);
    }

    @Test
    void refreshToken_Success_ReturnsNewToken() {
        when(jwtUtil.extractEmail("old-token")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken("test@test.com", 1, Role.USER)).thenReturn("new-token");

        String token = authService.refreshToken("old-token");

        assertEquals("new-token", token);
    }

    @Test
    void refreshToken_UserMissing_ThrowsException() {
        when(jwtUtil.extractEmail("old-token")).thenReturn("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.refreshToken("old-token"));
    }

    @Test
    void validateToken_DelegatesToJwtUtil() {
        when(jwtUtil.validateToken("abc")).thenReturn(true);
        assertTrue(authService.validateToken("abc"));
    }

    @Test
    void getUserById_UserMissing_ThrowsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.getUserById(99));
    }

    @Test
    void getUserByEmail_UserMissing_ThrowsException() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.getUserByEmail("missing@test.com"));
    }

    @Test
    void updateProfile_Success_UpdatesUser() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setAvatarUrl("avatar.png");
        request.setBio("bio");
        request.setTimezone("UTC");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = authService.updateProfile(1, request);

        assertEquals("Updated Name", updated.getFullName());
        assertEquals("avatar.png", updated.getAvatarUrl());
        assertEquals("bio", updated.getBio());
        assertEquals("UTC", updated.getTimezone());
    }

    @Test
    void changePassword_CurrentPasswordMismatch_ThrowsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("new-pass");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "hashedPassword123")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.changePassword(1, request));
    }

    @Test
    void changePassword_Success_EncodesAndSaves() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("plainPassword");
        request.setNewPassword("new-pass");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword123")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        authService.changePassword(1, request);

        assertEquals("new-hash", mockUser.getPasswordHash());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateCurrency_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        authService.updateCurrency(1, "USD");

        assertEquals("USD", mockUser.getCurrency());
        verify(userRepository).save(mockUser);
    }

    @Test
    void deactivateAccount_SetsInactive() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        authService.deactivateAccount(1);

        assertFalse(mockUser.isActive());
        verify(userRepository).save(mockUser);
    }

    @Test
    void adminOperations_ListAndCount() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(userRepository.findByIsActive(true)).thenReturn(List.of(mockUser));
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByIsActive(true)).thenReturn(7L);

        assertEquals(1, authService.getAllUsers().size());
        assertEquals(1, authService.getActiveUsers().size());
        Map<String, Long> counts = authService.getUserCount();
        assertEquals(10L, counts.get("total"));
        assertEquals(7L, counts.get("active"));
    }

    @Test
    void suspendAndReactivateUser_ToggleActiveFlag() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        authService.suspendUser(1);
        assertFalse(mockUser.isActive());

        authService.reactivateUser(1);
        assertTrue(mockUser.isActive());
        verify(userRepository, times(2)).save(mockUser);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(99)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> authService.deleteUser(99));
    }

    @Test
    void deleteUser_Success_DeletesById() {
        when(userRepository.existsById(1)).thenReturn(true);
        authService.deleteUser(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void upgradeAndSubscriptionStatus_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        authService.upgradeToPremium(1);
        assertEquals(SubscriptionType.PREMIUM, mockUser.getSubscriptionType());
        assertNotNull(mockUser.getPremiumExpiresAt());

        Map<String, Object> status = authService.getSubscriptionStatus(1);
        assertEquals(SubscriptionType.PREMIUM, status.get("subscriptionType"));
    }

    @Test
    void googleLogin_InvalidToken_ThrowsBadRequest() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setIdToken("invalid-token");

        assertThrows(BadRequestException.class, () -> authService.googleLogin(request));
    }
}