package com.backend.naildp.oauth2.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ErrorResponse {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final int code;
	private final String message;

	public String convertToJson() throws JsonProcessingException {
		return objectMapper.writeValueAsString(this);
	}

	public static ErrorResponse of(int status, String message) {
		return new ErrorResponse(status, message);
	}

}
