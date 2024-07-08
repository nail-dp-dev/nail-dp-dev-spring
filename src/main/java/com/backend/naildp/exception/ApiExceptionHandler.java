package com.backend.naildp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleExceptions(Exception exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<?>> handleExceptions(RuntimeException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(exception.getErrorCode());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.OK);
	}
}