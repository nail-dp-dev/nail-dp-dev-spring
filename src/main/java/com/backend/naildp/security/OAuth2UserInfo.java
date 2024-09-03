package com.backend.naildp.security;

import com.backend.naildp.common.ProviderType;

public interface OAuth2UserInfo {
	String getProviderId();

	ProviderType getProvider();

	String getEmail();

	String getImageUrl();
}