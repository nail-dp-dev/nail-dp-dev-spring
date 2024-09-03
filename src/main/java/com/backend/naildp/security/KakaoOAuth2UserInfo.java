package com.backend.naildp.security;

import java.util.Map;

import com.backend.naildp.common.ProviderType;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

	private Map<String, Object> attributes;

	public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public ProviderType getProvider() {
		return ProviderType.kakao;
	}

	@Override
	public String getProviderId() {
		return attributes.get("id").toString();
	}

	@Override
	public String getEmail() {
		return (String)attributes.get("account_email");
	}

	@Override
	public String getImageUrl() {
		Map<String, Object> properties = (Map<String, Object>)attributes.get("properties");

		if (properties == null) {
			return null;
		}

		return (String)properties.get("thumbnail_image");
	}
}
