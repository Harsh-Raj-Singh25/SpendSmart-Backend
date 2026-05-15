package com.spendsmart.auth.exception;

import com.spendsmart.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ErrorResponse errorResponse = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value())
				.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Validation Failed")
				.path(request.getRequestURI())
				.validationErrors(errors)
				.build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
		ErrorResponse errorResponse = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value())
				.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message(ex.getMessage())
				.path(request.getRequestURI())
				.build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	// Handles our custom BadRequestException (e.g., Duplicate Email)
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
		log.error("Bad Request: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message(ex.getMessage()).path(request.getRequestURI()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	// Handles our custom ResourceNotFoundException
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
			HttpServletRequest request) {
		log.error("Resource Not Found: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.NOT_FOUND.value()).error(HttpStatus.NOT_FOUND.getReasonPhrase())
				.message(ex.getMessage()).path(request.getRequestURI()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	// Handles Spring Security login failures (Invalid Email/Password)
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex,
			HttpServletRequest request) {
		log.warn("Failed login attempt: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.UNAUTHORIZED.value()).error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
				.message("Invalid email or password") // Keep message generic for security
				.path(request.getRequestURI()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	// Fallback for any other unhandled exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception occurred: ", ex);

		ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
				.message("An unexpected error occurred on the server.").path(request.getRequestURI()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}