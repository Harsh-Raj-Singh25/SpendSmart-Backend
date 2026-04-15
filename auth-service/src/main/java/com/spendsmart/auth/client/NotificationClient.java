package com.spendsmart.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// ============================================================================
// FEIGN CLIENT — Calls the notification-service to send emails.
//
// HOW FEIGN WORKS:
// 1. We declare an interface with @FeignClient and the target service name
// 2. We write method signatures matching the target service's endpoints
// 3. Spring Cloud auto-generates an HTTP client implementation at runtime
// 4. We inject this interface like any other Spring bean and call its methods
// 5. Eureka resolves "notification-service" to its actual URL (e.g., localhost:8088)
//
// USED BY: AuthServiceImpl for sending OTP emails during forgot-password flow.
// The notification-service has the SMTP config and JavaMailSender wired up.
// ============================================================================
@FeignClient(name = "notification-service")
public interface NotificationClient {

	// Sends an email via the notification-service's email dispatch endpoint.
	// This endpoint will be created in NotifResource.java.
	@PostMapping("/notifications/email")
	void sendEmail(
			@RequestParam("to") String to,
			@RequestParam("subject") String subject,
			@RequestParam("body") String body
	);
}
