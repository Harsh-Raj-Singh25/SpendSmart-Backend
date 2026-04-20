package com.spendsmart.notification;

import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.model.dto.NotificationRequest;
import com.spendsmart.notification.resource.NotifResource;
import com.spendsmart.notification.service.NotifService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifResourceTest {

    @Mock
    private NotifService notifService;

    @InjectMocks
    private NotifResource notifResource;

    @Test
    void allEndpointsDelegateToService() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .recipientId(5)
                .type("SYSTEM")
                .severity("INFO")
                .title("Hello")
                .message("World")
                .build();

        when(notifService.getByRecipient(5)).thenReturn(List.of(notification));
        assertEquals(1, notifResource.getByRecipient(5).getBody().size());

        when(notifService.getUnreadCount(5)).thenReturn(3);
        assertEquals(3, notifResource.getUnreadCount(5).getBody());

        assertEquals(200, notifResource.markAsRead(1).getStatusCode().value());
        assertEquals(200, notifResource.markAllRead(5).getStatusCode().value());
        assertEquals(200, notifResource.acknowledge(1).getStatusCode().value());
        assertEquals(204, notifResource.deleteNotification(1).getStatusCode().value());

        assertEquals(200, notifResource.sendBudgetAlert(5, "Limit", 100.0).getStatusCode().value());
        assertEquals(200, notifResource.sendBulk(List.of(5, 6), "Title", "Message").getStatusCode().value());

        NotificationRequest request = NotificationRequest.builder()
                .userId(5)
                .title("Reminder")
                .message("Pay bill")
                .type("IN_APP")
                .build();
        assertEquals(200, notifResource.sendNotification(request).getStatusCode().value());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifService).send(captor.capture());
        assertEquals(5, captor.getValue().getRecipientId());
        assertEquals("Reminder", captor.getValue().getTitle());

        assertEquals(200, notifResource.sendEmail("u@test.com", "Sub", "Body").getStatusCode().value());
        verify(notifService).sendDirectEmail("u@test.com", "Sub", "Body");
    }
}
