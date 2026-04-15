package com.spendsmart.notification.service;

import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotifServiceImpl implements NotifService {

	private final NotificationRepository notificationRepository;

	// Spring automatically injects this because of the dependency and YAML config
	private final JavaMailSender emailSender;

	// Grabs the email address from your YAML file so the "From" field is correct
	@Value("${spring.mail.username}")
	private String senderEmail;

	@Override
	public void send(Notification notification) {
		log.info("Saving new {} notification for Recipient ID: {}", notification.getSeverity(),
				notification.getRecipientId());

		notificationRepository.save(notification);

		if ("CRITICAL".equalsIgnoreCase(notification.getSeverity())) {
			log.warn("CRITICAL severity detected. Triggering email dispatch for Notification ID: {}",
					notification.getNotificationId());

			// In a fully integrated system, you would call Auth/User Service here to get
			// the user's real email.
			// For now, hardcode your own secondary email address here to test it!
			String targetEmail = "recipient.email@example.com";

			sendEmail(targetEmail, notification.getTitle(), notification.getMessage());
		}
	}

	@Override
	public void sendBudgetAlert(int recipientId, String title, double amount) {
		log.info("Generating domain-specific BUDGET_ALERT for Recipient ID: {} | Exceeded Amount: {}", recipientId,
				amount);

		Notification alert = Notification.builder().recipientId(recipientId).type("BUDGET_EXCEEDED")
				.severity("CRITICAL").title(title).message("Attention! You have exceeded your budget by " + amount
						+ ". Please review your expenses immediately.")
				.isRead(false).isAcknowledged(false).build();

		send(alert);
	}

	@Override
	public void sendBulk(List<Integer> recipientIds, String title, String message) {
		log.info("Initiating BULK notification dispatch to {} recipients.", recipientIds.size());

		for (Integer id : recipientIds) {
			Notification notification = Notification.builder().recipientId(id).type("SYSTEM").severity("INFO")
					.title(title).message(message).build();
			send(notification);
		}
	}

	@Override
	public void markAsRead(int notificationId) {
		Optional<Notification> opt = notificationRepository.findById(notificationId);
		opt.ifPresent(n -> {
			n.setRead(true);
			notificationRepository.save(n);
		});
	}

	@Override
	public void markAllRead(int recipientId) {
		List<Notification> unread = notificationRepository.findByRecipientIdAndIsRead(recipientId, false);
		unread.forEach(n -> n.setRead(true));
		notificationRepository.saveAll(unread);
	}

	@Override
	public void acknowledge(int notificationId) {
		Optional<Notification> opt = notificationRepository.findById(notificationId);
		opt.ifPresent(n -> {
			n.setRead(true);
			n.setAcknowledged(true);
			notificationRepository.save(n);
		});
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getByRecipient(int recipientId) {
		return notificationRepository.findByRecipientId(recipientId);
	}

	@Override
	@Transactional(readOnly = true)
	public int getUnreadCount(int recipientId) {
		return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
	}

	@Override
	public void deleteNotification(int notificationId) {
		notificationRepository.deleteByNotificationId(notificationId);
	}

	// --- The Actual Email Dispatch Method ---
	private void sendEmail(String to, String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(senderEmail);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);

			emailSender.send(message);
			log.info("Live email successfully sent to {}", to);

		} catch (Exception e) {
			// Catches connection timeouts, bad passwords, or invalid email formats
			log.error("CRITICAL: Failed to send actual email to {}. Error: {}", to, e.getMessage());
		}
	}
}