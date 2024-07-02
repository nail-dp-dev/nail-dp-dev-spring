package com.backend.naildp.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.backend.naildp.common.ApiResponse;
import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.dto.KakaoUserInfoDto;
import com.backend.naildp.jwt.JwtUtil;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserMapping;
import com.backend.naildp.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

	private final SocialLoginRepository socialLoginRepository;
	private final UserRepository userRepository;
	private final RestTemplate restTemplate;
	private final JwtUtil jwtUtil;
	private final CookieUtil cookieUtil;

	@Value("${kakao.client.id}") // Base64 Encode 한 SecretKey
	private String clientId;

	@Value("${kakao.redirect.uri}") // Base64 Encode 한 SecretKe
	private String redirectUri;

	public ApiResponse<?> kakaoLogin(String code, HttpServletResponse res) throws JsonProcessingException {
		log.info("인가코드 : " + code);
		// 인가 코드로 액세스 토큰 요청
		String accessToken = getToken(code);

		// 토큰으로 카카오 API 호출 : 액세스 토큰으로 카카오 사용자 정보 가져오기
		KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
		// DB 에 중복된 Kakao Id 가 있는지 확인
		Long kakaoId = kakaoUserInfo.getId();
		UserMapping kakaoUser = socialLoginRepository.findBySocialIdAndPlatform(kakaoId, "kakao").orElse(null);

		if (kakaoUser == null) { // 카카오 유저가 없다면
			// 홈페이지 신규 회원가입
			log.info("userInfo 쿠키 생성");
			cookieUtil.setUserInfoCookie(res, kakaoUserInfo);

			return ApiResponse.successWithLoginType("signUp", "회원가입 완료");

		} else {
			cookieUtil.deleteUserInfoCookie(res);

			log.info("jwt 쿠키 생성");
			String createToken = jwtUtil.createToken(kakaoUser.getUser().getNickname(), kakaoUser.getUser().getRole());
			jwtUtil.addJwtToCookie(createToken, res);

			return ApiResponse.successWithLoginType("login", "로그인 성공");

		}

	}

	private String getToken(String code) throws JsonProcessingException {
		// 요청 URL 만들기
		URI uri = UriComponentsBuilder.fromUriString("https://kauth.kakao.com")
			.path("/oauth/token")
			.encode()
			.build()
			.toUri();

		// HTTP Header 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// HTTP Body 생성
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", clientId);
		body.add("redirect_uri", redirectUri);
		body.add("code", code);

		RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity.post(uri)
			.headers(headers)
			.body(body);

		// HTTP 요청 보내기
		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

		// HTTP 응답 (JSON) -> 액세스 토큰 파싱
		JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
		return jsonNode.get("access_token").asText();
	}

	// 사용자 정보 가져오기
	private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
		log.info("accessToken : " + accessToken);

		// 요청 URL 만들기
		URI uri = UriComponentsBuilder.fromUriString("https://kapi.kakao.com")
			.path("/v2/user/me")
			.encode()
			.build()
			.toUri();

		// HTTP Header 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity.post(uri)
			.headers(headers)
			.body(new LinkedMultiValueMap<>());

		// HTTP 요청 보내기
		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

		JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
		Long id = jsonNode.get("id").asLong();
		// String nickname = jsonNode.get("properties")
		// 	.get("nickname").asText();
		String email = jsonNode.get("kakao_account").get("email").asText();

		String profileUrl = Optional.ofNullable(jsonNode.get("properties"))
			.map(properties -> properties.get("profile_image"))
			.map(JsonNode::asText)
			.orElse(null);

		log.info("카카오 사용자 정보: " + id + ", " + profileUrl + ", " + email);
		return new KakaoUserInfoDto(id, email, profileUrl);
	}

}