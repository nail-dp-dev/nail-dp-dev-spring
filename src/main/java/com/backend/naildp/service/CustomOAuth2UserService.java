package com.backend.naildp.service;

import java.util.Objects;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.backend.naildp.security.KakaoOAuth2UserInfo;
import com.backend.naildp.security.OAuth2UserInfo;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	// 구글로부터 받은 userRequest 데이터에 대한 후처리되는 함수
	// 함수 종료 시  @AuthenticationPrincipal 어노테이션이 생성
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);
		OAuth2UserInfo oAuth2UserInfo = null;

		if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
			oAuth2UserInfo = new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
		} else {
			System.out.println("지원하지않음.");
		}
		if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
			oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
		} else {
			System.out.println("Only Google");
		}

		String provider = Objects.requireNonNull(oAuth2UserInfo).getProvider();
		String providerId = oAuth2UserInfo.getProviderId();
		String username = provider + "_" + providerId;
		String password = "비밀번호";
		String email = oAuth2UserInfo.getEmail();
		String role = "RULE_USER";
		Timestamp createDate = new Timestamp(System.currentTimeMillis());

		User userEntity = userRepository.findByUsername(username);

		if (userEntity == null) {
			userEntity = User.builder()
				.username(username)
				.password(password)
				.email(email)
				.role(role)
				.provider(provider)
				.providerId(providerId)
				.enabled(1)
				.createDate(createDate)
				.build();
			userRepository.save(userEntity);
		}

		return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
	}
}
