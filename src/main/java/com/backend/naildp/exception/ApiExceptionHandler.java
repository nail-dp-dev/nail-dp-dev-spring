package com.backend.naildp.exception;

import static com.backend.naildp.exception.ErrorCode.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ApiResponse<?> handleExceptions(Exception exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(TEMPORARY_SERVER_ERROR);
		apiResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
		apiResponse.setMessage(exception.getMessage());
		return apiResponse;
	}

	@ExceptionHandler(value = CustomException.class)
	public ApiResponse<?> handleCustomException(CustomException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(exception.getErrorCode());
		apiResponse.setMessage(exception.getMessage());
		return apiResponse;
	}
}