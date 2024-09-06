package com.backend.naildp.oauth2;

import com.backend.naildp.common.ProviderType;

public interface OAuth2UserInfo {
	String getProviderId();

	ProviderType getProvider();

	String getEmail();

	String getImageUrl();
}