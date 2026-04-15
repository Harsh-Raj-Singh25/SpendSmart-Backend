package com.spendsmart.notification.repository;

import com.spendsmart.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

	List<Notification> findByRecipientId(int recipientId);

	List<Notification> findByRecipientIdAndIsRead(int recipientId, boolean isRead);

	int countByRecipientIdAndIsRead(int recipientId, boolean isRead);

	List<Notification> findByType(String type);

	List<Notification> findBySeverity(String severity);

	List<Notification> findByIsAcknowledged(boolean isAcknowledged);

	void deleteByNotificationId(int notificationId);
}