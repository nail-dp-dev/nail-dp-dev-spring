package com.backend.naildp.security;

import java.util.Map;

import com.backend.naildp.common.ProviderType;

public class OAuth2UserInfoFactory {
	public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
		switch (providerType) {
			// case GOOGLE: return new GoogleOAuth2UserInfo(attributes);
			// case NAVER: return new NaverOAuth2UserInfo(attributes);
			case KAKAO:
				return new KakaoOAuth2UserInfo(attributes);
			default:
				throw new IllegalArgumentException("Invalid Provider Type.");
		}
	}
}
