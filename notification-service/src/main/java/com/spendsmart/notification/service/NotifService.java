package com.spendsmart.notification.service;

import com.spendsmart.notification.entity.Notification;
import java.util.List;

public interface NotifService {
	void send(Notification notification);

	void sendBudgetAlert(int recipientId, String title, double amount);

	void sendBulk(List<Integer> recipientIds, String title, String message);

	void markAsRead(int notificationId);

	void markAllRead(int recipientId);

	void acknowledge(int notificationId);

	List<Notification> getByRecipient(int recipientId);

	int getUnreadCount(int recipientId);

	void deleteNotification(int notificationId);
}