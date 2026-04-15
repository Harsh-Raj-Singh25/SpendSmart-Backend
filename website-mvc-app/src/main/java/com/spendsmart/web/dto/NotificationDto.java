package com.spendsmart.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Integer notificationId;
    private String title;
    private String message;
    private String severity;
    private boolean isRead;
    private LocalDateTime createdAt;
}
