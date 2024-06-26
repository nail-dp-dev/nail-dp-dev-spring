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
	private String profile_url;

	public LoginRequestDto(String nickname, String phone_number, String profile_url) {
		this.nickname = nickname;
		this.phone_number = phone_number;
		this.profile_url = profile_url;
	}
}
