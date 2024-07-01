package com.backend.naildp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {
	private String nickname;
	private String phone_number;

	public LoginRequestDto(String nickname, String phone_number) {
		this.nickname = nickname;
		this.phone_number = phone_number;
	}
}
