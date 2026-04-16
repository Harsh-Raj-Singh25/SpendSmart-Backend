package com.spendsmart.payment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;

// ============================================================================
// GLOBAL EXCEPTION HANDLER for payment-service
//
// Without this, any RuntimeException (e.g., payment not found, Razorpay API
// failure, Feign connection error) bubbles up as an opaque 500 with a Spring
// Whitelabel error page. This handler converts them into clean JSON responses
// with a meaningful HTTP status code so the Angular frontend can display proper
// error messages.
// ============================================================================
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Handles payment business errors (signature mismatch, order not found, etc.) ──
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        log.error("Payment service error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Payment Error");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Catch-all for truly unexpected errors ──────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception in payment-service at {}: ", request.getRequestURI(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Check payment-service logs.");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
