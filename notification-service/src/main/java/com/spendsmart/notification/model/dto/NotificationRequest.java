package com.spendsmart.notification.model.dto;

import lombok.Builder;
import lombok.Data; 

@Data
@Builder
public class NotificationRequest {
	private Integer userId;
	private String title;
	private String message;
	private String type; // e.g., "EMAIL", "IN_APP", "BOTH"
}