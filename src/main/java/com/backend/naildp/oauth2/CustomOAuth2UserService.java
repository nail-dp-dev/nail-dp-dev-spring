package com.backend.naildp.oauth2;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.dto.auth.SocialUserInfoDto;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.SignUpRequiredException;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserMapping;
import com.backend.naildp.repository.UserRepository;

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
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);
		OAuth2UserInfo oAuth2UserInfo;
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		System.out.println("OAuth2 User Attributes: " + attributes);

		switch (registrationId) {
			case "kakao" -> oAuth2UserInfo = new KakaoOAuth2UserInfo(attributes);
			case "google" -> oAuth2UserInfo = new GoogleOAuth2UserInfo(attributes);
			case "naver" -> oAuth2UserInfo = new NaverOAuth2UserInfo(attributes);
			default -> {
				System.out.println("지원하지않음.");
				throw new OAuth2AuthenticationException("지원하지 않는 소셜로그인입니다.");
			}
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			User currentUser = ((UserDetailsImpl)authentication.getPrincipal()).getUser();
			return connectSocialAccount(currentUser, oAuth2UserInfo, oAuth2User);
		} else {
			return registerOrLoginSocialUser(oAuth2UserInfo, oAuth2User);
		}

	}

	private OAuth2User connectSocialAccount(User currentUser, OAuth2UserInfo oAuth2UserInfo, OAuth2User oAuth2User) {
		SocialLogin socialLogin = new SocialLogin(oAuth2UserInfo.getProviderId(), oAuth2UserInfo.getProvider(),
			oAuth2UserInfo.getEmail(), currentUser);
		socialLoginRepository.save(socialLogin);

		log.info("소셜 계정 연동 성공: {} -> {}", oAuth2UserInfo.getProvider(), currentUser.getNickname());

		return new UserDetailsImpl(currentUser, oAuth2User.getAttributes());

	}

	private OAuth2User registerOrLoginSocialUser(OAuth2UserInfo oAuth2UserInfo, OAuth2User oAuth2User) {
		UserMapping socialUser = socialLoginRepository.findBySocialIdAndPlatform(oAuth2UserInfo.getProviderId(),
			oAuth2UserInfo.getProvider()).orElse(null);

		if (socialUser == null) {
			log.info("userInfo 쿠키 생성");
			SocialUserInfoDto socialUserInfoDto = new SocialUserInfoDto(oAuth2UserInfo);
			cookieUtil.setUserInfoCookie(response, socialUserInfoDto);
			throw new SignUpRequiredException("회원가입이 필요합니다.");
		} else {
			cookieUtil.deleteCookie("userInfo", request, response);
			return new UserDetailsImpl(socialUser.getUser(), oAuth2User.getAttributes());
		}
	}
}
