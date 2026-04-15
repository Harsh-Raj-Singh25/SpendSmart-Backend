package com.spendsmart.notification.resource;

import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotifService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotifResource {

	private final NotifService notifService;

	// Standard retrieval
	@GetMapping("/recipient/{recipientId}")
	public ResponseEntity<List<Notification>> getByRecipient(@PathVariable int recipientId) {
		return ResponseEntity.ok(notifService.getByRecipient(recipientId));
	}

	@GetMapping("/recipient/{recipientId}/unread-count")
	public ResponseEntity<Integer> getUnreadCount(@PathVariable int recipientId) {
		return ResponseEntity.ok(notifService.getUnreadCount(recipientId));
	}

	// State mutations
	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<Void> markAsRead(@PathVariable int notificationId) {
		notifService.markAsRead(notificationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/recipient/{recipientId}/read-all")
	public ResponseEntity<Void> markAllRead(@PathVariable int recipientId) {
		notifService.markAllRead(recipientId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{notificationId}/acknowledge")
	public ResponseEntity<Void> acknowledge(@PathVariable int notificationId) {
		notifService.acknowledge(notificationId);
		return ResponseEntity.ok().build();
	}

	// Deletion
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> deleteNotification(@PathVariable int notificationId) {
		notifService.deleteNotification(notificationId);
		return ResponseEntity.noContent().build();
	}

	// Domain-Specific System Triggers (usually called by Feign, not directly by UI)
	@PostMapping("/budget-alert")
	public ResponseEntity<Void> sendBudgetAlert(@RequestParam int recipientId, @RequestParam String title,
			@RequestParam double amount) {
		notifService.sendBudgetAlert(recipientId, title, amount);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/bulk")
	public ResponseEntity<Void> sendBulk(@RequestBody List<Integer> recipientIds, @RequestParam String title,
			@RequestParam String message) {
		notifService.sendBulk(recipientIds, title, message);
		return ResponseEntity.ok().build();
	}
}