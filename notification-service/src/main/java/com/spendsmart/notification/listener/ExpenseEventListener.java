package com.spendsmart.notification.listener;

import java.time.LocalDateTime;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.spendsmart.event.ExpenseCreatedEvent;
import com.spendsmart.notification.config.RabbitMQConfig;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotifServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventListener {
	private final NotifServiceImpl notificationService;

//	Listens to the specific queue defined in RabbitMQConfig.
//    * Spring AMQP automatically deserializes the JSON back into an ExpenseCreatedEvent object!

	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void handleExpenseCreated(ExpenseCreatedEvent event) {
		log.info(" [RabbitMQ] Received ExpenseCreatedEvent: User {} spent ₹{} on '{}'", event.getUserId(),
				event.getAmount(), event.getTitle());

		try {
			// 1. Format a clean, user-friendly message
			String message = String.format("You logged a new expense: '%s' for ₹%s.", event.getTitle(),
					event.getAmount());

			// 2. Build the Notification entity (Assuming you are using a Builder pattern)
			Notification notification = Notification.builder().recipientId(event.getUserId()).message(message)
					.type("EXPENSE_LOGGED") // Helps the frontend decide what icon/color to show
					.severity("INFO")
					.title("Expense Logged")
					.isRead(false).createdAt(LocalDateTime.now()).build();

			// 3. Save it to the Notification Service database
//			notificationService.saveNotification(notification);
			notificationService.send(notification); 
			log.info(" Successfully processed and saved notification for User {}", event.getUserId());

		} catch (Exception e) {
			log.error("Failed to process ExpenseCreatedEvent for User {}. Routing to DLQ.", event.getUserId(), e);
			throw new AmqpRejectAndDontRequeueException("Failed to process ExpenseCreatedEvent", e);
		}
	}

}
