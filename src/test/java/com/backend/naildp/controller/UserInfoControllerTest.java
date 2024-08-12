package com.backend.naildp.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.UserInfoService;

@WebMvcTest(UserInfoController.class)
public class UserInfoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserInfoService userInfoService;

	@Mock
	private UserDetailsImpl userDetails;

	private User user;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		user = User.builder()
			.nickname("testUser")
			.phoneNumber("010-1234-5678")
			.role(UserRole.USER)
			.agreement(true)
			.build();

		given(userDetails.getUser()).willReturn(user);

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(new TestingAuthenticationToken(userDetails, null, "ROLE_USER"));
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	@DisplayName("사용자 정보 조회")
	public void testGetUserInfo() throws Exception {
		// Given
		UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.builder()
			.nickname("testUser")
			.point(100L)
			.profileUrl("profile.jpg")
			.postsCount(5)
			.saveCount(2)
			.followerCount(10)
			.build();

		given(userInfoService.getUserInfo("testUser")).willReturn(userInfoResponseDto);

		//Then
		mockMvc.perform(get("/user").with(csrf())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nickname").value("testUser"))
			.andExpect(jsonPath("$.data.point").value(100L))
			.andExpect(jsonPath("$.data.profileUrl").value("profile.jpg"))
			.andExpect(jsonPath("$.data.postsCount").value(5))
			.andExpect(jsonPath("$.data.saveCount").value(2))
			.andExpect(jsonPath("$.data.followerCount").value(10))
			.andExpect(jsonPath("$.message").value("사용자 정보 조회 성공"))
			.andExpect(jsonPath("$.code").value(2000));
	}

	@Test
	@DisplayName("사용자 정보 조회 - 예외")
	public void testGetUserInfoNotFound() throws Exception {
		// Given
		doThrow(new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND))
			.when(userInfoService).getUserInfo(anyString());

		// Then
		mockMvc.perform(get("/user").with(csrf())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("nickname 으로 회원을 찾을 수 없습니다."))
			.andExpect(jsonPath("$.code").value(4002));
	}

	@Test
	@DisplayName("사용자 정보 조회 - 썸네일 예외")
	public void testGetUserInfoThumbnailNotFound() throws Exception {
		// Given
		doThrow(new CustomException("설정된 프로필 썸네일이 없습니다.", ErrorCode.NOT_FOUND))
			.when(userInfoService).getUserInfo(anyString());

		// Then
		mockMvc.perform(get("/user").with(csrf())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("설정된 프로필 썸네일이 없습니다."))
			.andExpect(jsonPath("$.code").value(4002));
	}
}