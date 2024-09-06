package com.backend.naildp.exception;

import org.springframework.security.core.AuthenticationException;

public class SignUpRequiredException extends AuthenticationException {

	public SignUpRequiredException(String message) {
		super(message);
	}

	public SignUpRequiredException(String message, Throwable cause) {
		super(message, cause);
	}
}
