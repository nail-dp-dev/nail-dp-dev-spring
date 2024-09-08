package com.backend.naildp.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.auth.NicknameRequestDto;
import com.backend.naildp.dto.auth.PhoneNumberRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

	@MockBean
	private OAuth2AuthorizedClientService authorizedClientService;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	private ObjectMapper objectMapper;
	private LoginRequestDto loginRequestDto;
	private NicknameRequestDto nicknameRequestDto;
	private PhoneNumberRequestDto phoneNumberRequestDto;

	@BeforeEach
	public void setUp() {
		objectMapper = new ObjectMapper();

		loginRequestDto = new LoginRequestDto();
		loginRequestDto.setNickname("alswl123");
		loginRequestDto.setPhoneNumber("010-1234-5678");

		nicknameRequestDto = new NicknameRequestDto();
		nicknameRequestDto.setNickname("alswl23");

		phoneNumberRequestDto = new PhoneNumberRequestDto();
		phoneNumberRequestDto.setPhoneNumber("010-1234-5678");
	}

	@Test
	@DisplayName("회원가입 성공")
	@WithMockUser
	public void testSignupUser() throws Exception {
		// given
		given(authService.signupUser(any(LoginRequestDto.class), any(HttpServletRequest.class),
			any(HttpServletResponse.class)))
			.willReturn(ResponseEntity.ok().body(ApiResponse.successResponse(null, "회원가입 완료", 2001)));

		// when & then
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("회원가입 완료"))
			.andExpect(jsonPath("$.code").value(2001))
			.andDo(print());
	}

	// @Test
	// @DisplayName("카카오 로그인 성공")
	// public void testKakaoLogin() throws Exception {
	// 	// given
	// 	String code = "authCode";
	// 	given(kakaoService.kakaoLogin(eq(code), any(HttpServletRequest.class), any(HttpServletResponse.class)))
	// 		.willReturn(ResponseEntity.ok().body(ApiResponse.successResponse(null, "로그인이 완료되었습니다", 2000)));
	//
	// 	// when & then
	// 	mockMvc.perform(get("/auth/kakao")
	// 			.param("code", code))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.message").value("로그인이 완료되었습니다"))
	// 		.andExpect(jsonPath("$.code").value(2000))
	// 		.andDo(print());
	// }

	@Test
	@DisplayName("닉네임 중복 확인 성공")
	public void testDuplicateNickname() throws Exception {
		// given
		given(authService.duplicateNickname(any(NicknameRequestDto.class)))
			.willReturn(ResponseEntity.ok().body(ApiResponse.successResponse(null, "사용 가능한 닉네임입니다", 2000)));

		// when & then
		mockMvc.perform(post("/auth/nickname")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(nicknameRequestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다"))
			.andExpect(jsonPath("$.code").value(2000))
			.andDo(print());
	}

	@Test
	@DisplayName("닉네임 유효성 검사 실패 - 빈 문자열")
	public void testDuplicateNickname_Dto_fail1() throws Exception {
		// given
		NicknameRequestDto nicknameRequestDto = new NicknameRequestDto();
		nicknameRequestDto.setNickname("");

		// when & then
		mockMvc.perform(post("/auth/nickname")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(nicknameRequestDto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("닉네임을 입력해주세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("닉네임 유효성 검사 실패 - 특수문자 포함")
	public void testDuplicateNickname_Dto_fail2() throws Exception {
		// given
		NicknameRequestDto invalidNicknameRequest = new NicknameRequestDto();
		invalidNicknameRequest.setNickname("alswl!@#");

		// when & then
		mockMvc.perform(post("/auth/nickname")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidNicknameRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("닉네임은 특수문자를 제외한 4~15자리여야 합니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("전화번호 중복 확인 성공")
	public void testDuplicatePhone() throws Exception {
		// given
		given(authService.duplicatePhone(any(PhoneNumberRequestDto.class)))
			.willReturn(ResponseEntity.ok().body(ApiResponse.successResponse(null, "사용 가능한 전화번호입니다", 2000)));

		// when & then
		mockMvc.perform(post("/auth/phone")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(phoneNumberRequestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("사용 가능한 전화번호입니다"))
			.andExpect(jsonPath("$.code").value(2000))
			.andDo(print());
	}

	@Test
	@DisplayName("JWT 쿠키 확인 성공")
	public void testCheckCookie() throws Exception {
		// given
		given(authService.checkCookie(any(HttpServletRequest.class)))
			.willReturn(ResponseEntity.ok().body(ApiResponse.successResponse(null, "jwt토큰 검증 확인", 2000)));

		// when & then
		mockMvc.perform(get("/auth/cookie"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("jwt토큰 검증 확인"))
			.andExpect(jsonPath("$.code").value(2000))
			.andDo(print());
	}

	@Test
	@DisplayName("로그아웃 성공")
	public void testLogoutUser() throws Exception {
		// given
		given(authService.logoutUser(any(HttpServletRequest.class), any(HttpServletResponse.class)))
			.willReturn(ResponseEntity.ok().body(ApiResponse.successResponse(null, "로그아웃 성공", 2000)));

		// when & then
		mockMvc.perform(get("/auth/logout"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("로그아웃 성공"))
			.andExpect(jsonPath("$.code").value(2000))
			.andDo(print());
	}
}