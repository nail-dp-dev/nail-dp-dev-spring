package com.backend.naildp.dto.auth;

import com.backend.naildp.common.ProviderType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoDto {
	private Long id;
	private String email;
	private String profileUrl;
	private ProviderType platform;
}