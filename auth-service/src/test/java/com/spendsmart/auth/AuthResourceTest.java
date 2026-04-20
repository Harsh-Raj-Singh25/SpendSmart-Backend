package com.spendsmart.auth;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.exception.BadRequestException;
import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.model.enums.SubscriptionType;
import com.spendsmart.auth.resource.AuthResource;
import com.spendsmart.auth.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthResource authResource;

    @AfterEach
    void cleanupContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicEndpointsDelegateSuccessfully() {
        RegisterRequest registerRequest = new RegisterRequest();
        LoginRequest loginRequest = new LoginRequest();
        GoogleAuthRequest googleAuthRequest = new GoogleAuthRequest();
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt")
                .userId(5)
                .fullName("User")
                .email("user@test.com")
                .role(Role.USER)
                .subscriptionType(SubscriptionType.FREE)
                .build();

        when(authService.register(registerRequest)).thenReturn(authResponse);
        when(authService.login(loginRequest)).thenReturn(authResponse);
        when(authService.googleLogin(googleAuthRequest)).thenReturn(authResponse);

        assertEquals("jwt", authResource.register(registerRequest).getBody().getToken());
        assertEquals("jwt", authResource.login(loginRequest).getBody().getToken());
        assertEquals("jwt", authResource.googleLogin(googleAuthRequest).getBody().getToken());

        assertEquals(200, authResource.forgotPassword(forgotRequest).getStatusCode().value());
        assertEquals(200, authResource.resetPassword(resetRequest).getStatusCode().value());

        verify(authService).forgotPassword(forgotRequest);
        verify(authService).resetPassword(resetRequest);
    }

    @Test
    void authenticatedEndpointsDelegateSuccessfully() {
        User user = User.builder().userId(5).email("user@test.com").build();
        when(authService.getUserById(5)).thenReturn(user);
        when(authService.refreshToken("token")).thenReturn("new-token");
        when(authService.getSubscriptionStatus(5)).thenReturn(Map.of("subscriptionType", "PREMIUM"));

        assertEquals(200, authResource.logout("Bearer token").getStatusCode().value());
        assertEquals("new-token", authResource.refresh("Bearer token").getBody().get("token"));
        assertEquals("user@test.com", authResource.getProfile(5).getBody().getEmail());
        assertEquals(200, authResource.upgradeToPremium(5).getStatusCode().value());
        assertEquals("PREMIUM", authResource.getSubscriptionStatus(5).getBody().get("subscriptionType"));
        assertEquals("user@test.com", authResource.getUserEmail(5).getBody().get("email"));

        verify(authService).logout("token");
        verify(authService).upgradeToPremium(5);
    }

    @Test
    void profileMutationsRequireMatchingAuthenticatedUser() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "n/a");
        auth.setDetails(10);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();

        assertThrows(BadRequestException.class, () -> authResource.updateProfile(5, updateProfileRequest));
        assertThrows(BadRequestException.class, () -> authResource.changePassword(5, changePasswordRequest));
        assertThrows(BadRequestException.class, () -> authResource.updateCurrency(5, "USD"));
        assertThrows(BadRequestException.class, () -> authResource.deactivateAccount(5));
    }

    @Test
    void profileMutationsSucceedForOwner() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "n/a");
        auth.setDetails(5);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();

        User updatedUser = User.builder().userId(5).fullName("Updated").build();
        when(authService.updateProfile(5, updateProfileRequest)).thenReturn(updatedUser);

        assertEquals("Updated", authResource.updateProfile(5, updateProfileRequest).getBody().getFullName());
        assertEquals(200, authResource.changePassword(5, changePasswordRequest).getStatusCode().value());
        assertEquals(200, authResource.updateCurrency(5, "USD").getStatusCode().value());
        assertEquals(200, authResource.deactivateAccount(5).getStatusCode().value());

        verify(authService).changePassword(5, changePasswordRequest);
        verify(authService).updateCurrency(5, "USD");
        verify(authService).deactivateAccount(5);
    }
}
