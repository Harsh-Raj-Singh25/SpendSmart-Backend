package com.spendsmart.auth.dto;

import lombok.Data;

//LoginRequest.java
@Data
public class LoginRequest {
	private String email;
	private String password;
}