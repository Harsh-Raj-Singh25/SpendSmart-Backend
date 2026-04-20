package com.spendsmart.notification.service;

import com.spendsmart.notification.client.AuthClient;
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
	private final AuthClient authClient;

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
			// getting the mail of the recipient using the auth client
			String targetEmail = resolveRecipientEmail(notification.getRecipientId());
			if (targetEmail != null && !targetEmail.isBlank()) {
				sendEmail(targetEmail, notification.getTitle(), notification.getMessage());
			} else {
				log.warn("Skipping CRITICAL email dispatch. No email found for Recipient ID: {}",
						notification.getRecipientId());
			}
		}
	}
	// Helper method to resolve recipient's email via auth-service Feign client
	private String resolveRecipientEmail(Integer recipientId) {
		if (recipientId == null) {
			return null;
		}
		try {
			var response = authClient.getUserEmail(recipientId);
			return response != null ? response.get("email") : null;
		} catch (Exception e) {
			log.error("Failed to resolve email for Recipient ID {} via auth-service: {}", recipientId, e.getMessage());
			return null;
		}
	}

	@Override
	public void sendBudgetAlert(int recipientId, String title, double amount) {
		log.info("Generating domain-specific BUDGET_ALERT for Recipient ID: {} | Exceeded Amount: {}", recipientId,
				amount);

		String message;
		if (amount > 0) {
			message = "Attention! You have exceeded your budget by " + String.format("%.2f", amount)
					+ ". Please review your expenses immediately.";
		} else {
			message = "Heads up! You are approaching your budget limit (above 85% usage). "
					+ "Please monitor your upcoming expenses.";
		}

		Notification alert = Notification.builder().recipientId(recipientId).type("BUDGET_EXCEEDED")
				.severity("CRITICAL").title(title).message(message)
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

	// ── PUBLIC EMAIL DISPATCH ───────────────────────────────────────────
	// Called by auth-service (via Feign) for sending OTP password reset emails.
	// This is a direct email send without creating an in-app notification.
	@Override
	public void sendDirectEmail(String to, String subject, String body) {
		log.info("Sending direct email to: {}", to);
		sendEmail(to, subject, body);
	}

	// --- The Actual Email Dispatch Method ---
	private void sendEmail(String to, String subject, String text) {
		try {
			String finalSubject = buildImpressiveSubject(subject, text);
			String finalBody = buildImpressiveBody(to, subject, text);

			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(senderEmail);
			message.setTo(to);
			message.setSubject(finalSubject);
			message.setText(finalBody);

			emailSender.send(message);
			log.info("Live email successfully sent to {}", to);

		} catch (Exception e) {
			// Catches connection timeouts, bad passwords, or invalid email formats
			log.error("CRITICAL: Failed to send actual email to {}. Error: {}", to, e.getMessage());
		}
	}

	private String buildImpressiveSubject(String subject, String body) {
		String safeSubject = subject == null ? "SpendSmart Notification" : subject.trim();
		String context = (safeSubject + " " + (body == null ? "" : body)).toLowerCase();

		if (context.contains("otp") || context.contains("password reset")) {
			return "[SpendSmart Security] " + safeSubject;
		}
		if (context.contains("budget")) {
			return "[SpendSmart Insights] " + safeSubject;
		}
		return "[SpendSmart] " + safeSubject;
	}

	private String buildImpressiveBody(String to, String subject, String body) {
		String safeBody = body == null ? "" : body.trim();
		String displayName = deriveDisplayNameFromEmail(to);
		String context = ((subject == null ? "" : subject) + " " + safeBody).toLowerCase();

		String intro;
		String guidance;
		if (context.contains("otp") || context.contains("password reset")) {
			intro = "A secure action was initiated on your SpendSmart account.";
			guidance = "For your safety, never share this code with anyone. SpendSmart support will never ask for your OTP.";
		} else if (context.contains("budget")) {
			intro = "Your financial assistant has detected an important budget update.";
			guidance = "Quick tip: review this category and rebalance upcoming expenses to stay on track this month.";
		} else {
			intro = "Here is an important update from your SpendSmart account.";
			guidance = "Open SpendSmart for full details and recommended next actions.";
		}

		return "Hello " + displayName + ",\n\n"
				+ intro + "\n\n"
				+ safeBody + "\n\n"
				+ guidance + "\n\n"
				+ "Thank you for trusting SpendSmart to manage your financial journey.\n"
				+ "- Team SpendSmart";
	}

	private String deriveDisplayNameFromEmail(String email) {
		if (email == null || email.isBlank() || !email.contains("@")) {
			return "there";
		}
		String localPart = email.substring(0, email.indexOf('@')).trim();
		if (localPart.isEmpty()) {
			return "there";
		}
		String cleaned = localPart.replace('.', ' ').replace('_', ' ').replace('-', ' ').trim();
		if (cleaned.isEmpty()) {
			return "there";
		}
		return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
	}
}