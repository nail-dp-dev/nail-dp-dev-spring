package com.backend.naildp.oauth2;

import java.util.Map;

import com.backend.naildp.common.ProviderType;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

	private Map<String, Object> attributes;

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public ProviderType getProvider() {
		return ProviderType.google;
	}

	@Override
	public String getProviderId() {
		return attributes.get("sub").toString();
	}

	@Override
	public String getEmail() {
		return (String)attributes.get("email");
	}

	@Override
	public String getImageUrl() {
		return (String)attributes.get("picture");
	}
}
