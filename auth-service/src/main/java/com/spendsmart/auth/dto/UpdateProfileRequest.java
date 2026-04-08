package com.spendsmart.auth.dto;

import lombok.Data;

//UpdateProfileRequest.java
@Data
public class UpdateProfileRequest {
	private String fullName;
	private String avatarUrl;
	private String bio;
	private String timezone;
}
