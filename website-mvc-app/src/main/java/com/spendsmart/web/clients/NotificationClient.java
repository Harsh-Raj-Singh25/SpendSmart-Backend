package com.spendsmart.web.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; 

import com.spendsmart.web.dto.NotificationDto; 

@FeignClient(name = "notification-client", url = "http://localhost:8080/notifications")
public interface NotificationClient {
    @GetMapping("/recipient/{userId}/unread-count")
    Integer getUnreadCount(@PathVariable("userId") int userId);
    
    @GetMapping("/recipient/{userId}")
    List<NotificationDto> getUserNotifications(@PathVariable("userId") int userId);
}