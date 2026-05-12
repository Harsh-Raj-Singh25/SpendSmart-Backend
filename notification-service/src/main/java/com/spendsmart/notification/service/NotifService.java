package com.spendsmart.notification.service;

import com.spendsmart.notification.entity.Notification;
import java.util.List;

public interface NotifService {
	void send(Notification notification);

	List<Notification> getAllNotifications();

	void sendBudgetAlert(int recipientId, String title, double amount);

	void sendBulk(List<Integer> recipientIds, String title, String message);

	void markAsRead(int notificationId);

	void markAllRead(int recipientId);

	void acknowledge(int notificationId);

	List<Notification> getByRecipient(int recipientId);

	int getUnreadCount(int recipientId);

	Notification getNotificationById(int notificationId);

	void deleteNotification(int notificationId);

	// Sends a raw email without creating an in-app notification.
	// Used by auth-service for OTP password reset emails.
	void sendDirectEmail(String to, String subject, String body);
}