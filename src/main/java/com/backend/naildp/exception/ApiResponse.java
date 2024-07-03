package com.backend.naildp.exception;

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
	private int code;

	public static <T> ApiResponse<T> successResponse(T data, String message, int code) {
		return new ApiResponse<>(data, message, code);
	}

	private ApiResponse(T data, String message, int code) {
		this.data = data;
		this.message = message;
		this.code = code;
	}

	public ApiResponse(ErrorCode code) {
		this.code = code.getErrorCode();
	}

	public static ApiResponse<?> of(ErrorCode code) {
		return new ApiResponse<>(code);
	}

	public static ApiResponse<?> of() {
		return new ApiResponse<>();
	}

}