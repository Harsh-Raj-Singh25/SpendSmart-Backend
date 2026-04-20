package com.spendsmart.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
//Feign client for auth email lookup
@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/auth/internal/users/{userId}/email")
    Map<String, String> getUserEmail(@PathVariable("userId") int userId);
}
