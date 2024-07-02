package com.backend.naildp.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

	private T data;
	private String message;
	private HttpStatus httpStatus;

	private String code;

	public static <T> ApiResponse<T> successResponse(HttpStatus status, T data, String message, String code) {
		return new ApiResponse<>(status, data, message, code);
	}

	private ApiResponse(HttpStatus status, T data, String message, String code) {
		this.httpStatus = status;
		this.data = data;
		this.message = message;
		this.code = code;
	}

	public ApiResponse(ErrorCode code) {
		this.httpStatus = code.getHttpStatus();
		this.code = code.getErrorCode();
		this.message = code.getMessage();
	}

	public static ApiResponse<?> of(ErrorCode code) {
		return new ApiResponse<>(code);
	}

}