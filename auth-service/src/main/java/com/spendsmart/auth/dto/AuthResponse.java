package com.spendsmart.auth.dto;

import com.spendsmart.auth.model.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

//AuthResponse.java
@Data
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private int userId;
	private String fullName;
	private String email;
	private Role role;
}