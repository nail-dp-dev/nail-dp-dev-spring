package com.backend.naildp.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.jwt.JwtUtil;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserMapping;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.security.KakaoOAuth2UserInfo;
import com.backend.naildp.security.OAuth2UserInfo;
import com.backend.naildp.security.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@RequestScope
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final SocialLoginRepository socialLoginRepository;
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	private final CookieUtil cookieUtil;
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws
		OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);
		OAuth2UserInfo oAuth2UserInfo = null;
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		if (registrationId.equals("kakao")) {
			oAuth2UserInfo = new KakaoOAuth2UserInfo((Map<String, Object>)oAuth2User.getAttributes().get("id"));
		} else {
			System.out.println("지원하지않음.");
		}

		UserMapping socialUser = socialLoginRepository.findBySocialIdAndPlatform(
			oAuth2UserInfo.getProviderId(), oAuth2UserInfo.getProvider()).orElse(null);

		if (socialUser == null) { // 해당 소셜 유저가 없을 경우
			log.info("userInfo 쿠키 생성");
			cookieUtil.setUserInfoCookie(response, oAuth2UserInfo);

			throw new OAuth2AuthenticationException("소셜 계정이 연결된 회원이 없습니다. 회원 가입이 필요합니다.");
		}

		log.info("jwt 쿠키 생성");
		cookieUtil.deleteCookie("userInfo", request, response);

		String token = jwtUtil.createToken(socialUser.getUser().getNickname(),
			socialUser.getUser().getRole());
		jwtUtil.addJwtToCookie(token, response);

		return new UserDetailsImpl(socialUser.getUser(), oAuth2User.getAttributes());
	}

}
