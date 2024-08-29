package com.backend.naildp.security;

public interface OAuth2UserInfo {
	String getProviderId();

	String getProvider();

	String getEmail();

	String getUsername();
}