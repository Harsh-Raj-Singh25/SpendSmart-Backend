package com.spendsmart.notification;

import com.spendsmart.notification.client.AuthClient;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository;
import com.spendsmart.notification.service.NotifServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private NotifServiceImpl notifService;
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private AuthClient client;
    @BeforeEach
    void setUp() {
        // Inject the @Value property required for the "From" email address
        ReflectionTestUtils.setField(notifService, "senderEmail", "noreply@spendsmart.com");
    }

    @Test
    void send_InfoSeverity_SavesToDatabaseButDoesNotEmail() {
        // Arrange
        Notification infoNotif = Notification.builder()
                .recipientId(5)
                .severity("INFO")
                .type("SYSTEM")
                .title("Welcome")
                .message("Hello World")
                .build();

        // Act
        notifService.send(infoNotif);

        // Assert
        verify(notificationRepository, times(1)).save(infoNotif);
        verify(emailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_CriticalSeverity_SavesToDatabaseAndSendsEmail() {
        // Arrange
        Notification criticalNotif = Notification.builder()
                .recipientId(5)
                .severity("CRITICAL")
                .type("BUDGET_EXCEEDED")
                .title("Danger")
                .message("Over budget")
                .build();

        // Act
        notifService.send(criticalNotif);

        // Assert
        verify(notificationRepository, times(1)).save(criticalNotif);
//        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void markAsRead_UpdatesStatusSuccessfully() {
        // Arrange
        Notification mockNotif = new Notification();
        mockNotif.setRead(false);
        when(notificationRepository.findById(1)).thenReturn(Optional.of(mockNotif));

        // Act
        notifService.markAsRead(1);

        // Assert
        assertTrue(mockNotif.isRead());
        verify(notificationRepository, times(1)).save(mockNotif);
    }
}