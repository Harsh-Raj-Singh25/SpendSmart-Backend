package com.spendsmart.recurring.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
	private Integer userId;
	private String title;
    private String message;
    private String type; // "EMAIL", "IN_APP", or "BOTH"
}
