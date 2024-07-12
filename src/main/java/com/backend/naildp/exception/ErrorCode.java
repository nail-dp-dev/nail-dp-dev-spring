package com.backend.naildp.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum ErrorCode {
	// 3xx Redirection
	MOVED_TEMPORARILY(302),

	// 400 Bad Request
	TEMPORARY_SERVER_ERROR(400),

	INPUT_NULL(4000),

	ALREADY_EXIST(4001),

	// 404 Not Found
	NOT_FOUND(4002),

	INVALID_FILE_EXTENSION(4003),

	FILE_EXCEPTION(4004);

	// EXPIRED_JWT("401_1", "JWT 시간이 만료되었습니다."),
	// AUTHENTICATION_FAILURE_JWT("401_2", "올바른 JWT 정보가 아닙니다.");
	private final int errorCode;

}