package com.backend.naildp.entity;

import com.backend.naildp.dto.KakaoUserInfoDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialLogin {

	@Id
	@Column(name = "login_id")
	private Long socialId;

	@Column(nullable = false)
	private String platform;

	@Column(nullable = false)
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public SocialLogin(KakaoUserInfoDto kakaoUserInfo, User user) {
		this.socialId = kakaoUserInfo.getId();
		this.email = kakaoUserInfo.getEmail();
		this.platform = kakaoUserInfo.getPlatform();
		this.user = user;
	}
}
