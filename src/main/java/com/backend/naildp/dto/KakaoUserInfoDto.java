package com.backend.naildp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {
	private Long id;
	private String email;
	private final String platform = "kakao";

	public KakaoUserInfoDto(Long id, String email) {
		this.id = id;
		this.email = email;
	}
}