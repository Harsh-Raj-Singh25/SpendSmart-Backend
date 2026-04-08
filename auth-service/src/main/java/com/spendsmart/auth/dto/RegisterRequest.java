package com.spendsmart.auth.dto;
//RegisterRequest.java 

import lombok.Data;

@Data
public class RegisterRequest {
	private String fullName;
	private String email;
	private String password;
}