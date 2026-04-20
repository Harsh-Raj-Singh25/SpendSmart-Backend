package com.spendsmart.auth;

import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.resource.AdminResource;
import com.spendsmart.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminResourceTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminResource adminResource;

    @Test
    void adminEndpointsDelegateToService() {
        User user = User.builder().userId(1).email("admin@test.com").build();
        when(authService.getAllUsers()).thenReturn(List.of(user));
        when(authService.getActiveUsers()).thenReturn(List.of(user));
        when(authService.getUserCount()).thenReturn(Map.of("total", 1L, "active", 1L));
        when(authService.getUserById(1)).thenReturn(user);

        assertEquals(1, adminResource.getAllUsers().getBody().size());
        assertEquals(1, adminResource.getActiveUsers().getBody().size());
        assertEquals(1L, adminResource.getUserCount().getBody().get("total"));
        assertEquals("admin@test.com", adminResource.getUserById(1).getBody().getEmail());

        assertEquals(200, adminResource.suspendUser(1).getStatusCode().value());
        assertEquals(200, adminResource.reactivateUser(1).getStatusCode().value());
        assertEquals(200, adminResource.deleteUser(1).getStatusCode().value());

        verify(authService).suspendUser(1);
        verify(authService).reactivateUser(1);
        verify(authService).deleteUser(1);
    }
}
