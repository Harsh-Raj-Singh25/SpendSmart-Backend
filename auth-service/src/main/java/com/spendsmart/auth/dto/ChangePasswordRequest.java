package com.spendsmart.auth.dto;

import lombok.Data;

//ChangePasswordRequest.java
@Data
public class ChangePasswordRequest {
	private String currentPassword;
	private String newPassword;
}