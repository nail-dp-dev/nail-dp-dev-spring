package com.backend.naildp.dto.auth;

import com.backend.naildp.common.ProviderType;
import com.backend.naildp.oauth2.OAuth2UserInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfoDto {
	private String id;
	private String email;
	private String profileUrl;
	private ProviderType platform;

	public SocialUserInfoDto(OAuth2UserInfo userinfo) {
		this.id = userinfo.getProviderId();
		this.email = userinfo.getEmail();
		this.profileUrl = userinfo.getImageUrl();
		this.platform = userinfo.getProvider();
	}

}