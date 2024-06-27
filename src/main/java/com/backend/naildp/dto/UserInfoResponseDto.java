package com.backend.naildp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoResponseDto {
	boolean success;
	String message;
	KakaoUserInfoDto kakaoUserInfoDto;
}
