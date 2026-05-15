package com.spendsmart.auth.dto;

import lombok.Data;

//UpdateProfileRequest.java
@Data
public class UpdateProfileRequest {
	@jakarta.validation.constraints.NotBlank(message = "Full name is required")
	private String fullName;
	private String avatarUrl;
	private String bio;
	private String timezone;
}
