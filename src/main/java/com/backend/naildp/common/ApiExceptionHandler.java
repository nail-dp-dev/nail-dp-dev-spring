package com.backend.naildp.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"com.test.apihandler"})
public class ApiExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleExceptions(RuntimeException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.errorResponse(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidationExceptions(BindingResult bindingResult) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failResponse(bindingResult));
	}
}