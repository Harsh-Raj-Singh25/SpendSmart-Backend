package com.spendsmart.recurring.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.spendsmart.recurring.model.dto.NotificationRequest;
// Note: You must recreate the NotificationRequest DTO class inside the recurring-service to use here!

@FeignClient(name = "notification-service")
public interface NotificationClient {
	@PostMapping("/notifications/send")
	void sendNotification(@RequestBody NotificationRequest request);  
}