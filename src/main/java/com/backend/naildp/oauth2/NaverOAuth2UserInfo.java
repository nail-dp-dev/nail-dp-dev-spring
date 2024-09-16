package com.backend.naildp.oauth2;

import java.util.Map;

import com.backend.naildp.common.ProviderType;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

	private Map<String, Object> attributes;

	public NaverOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public ProviderType getProvider() {
		return ProviderType.naver;
	}

	@Override
	public String getProviderId() {
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");
		return response.get("id").toString();
	}

	@Override
	public String getEmail() {
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");
		return (String)response.get("email");
	}

	@Override
	public String getImageUrl() {
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");
		if (response.get("profile_image") == null) {
			return null;
		}
		return (String)response.get("profile_image");
	}
}
