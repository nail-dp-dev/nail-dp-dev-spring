package com.backend.naildp.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.archive.ArchiveBoundaryRequestDto;
import com.backend.naildp.dto.archive.ArchiveNameRequestDto;
import com.backend.naildp.dto.archive.CreateArchiveRequestDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.ArchiveService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ArchiveController.class)
public class ArchiveControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ArchiveService archiveService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
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
	@DisplayName("아카이브 생성 성공")
	void testCreateArchiveSuccessfully() throws Exception {
		// Given
		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("My Arch", Boundary.ALL);
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(post("/archive").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))

			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("새 아카이브 생성 성공"))
			.andExpect(jsonPath("$.code").value(2001));
	}

	@Test
	@DisplayName("아카이브 생성 실패-notBlank")
	void testCreateArchiveFail1() throws Exception {
		// Given
		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("", Boundary.ALL);
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(post("/archive").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("닉네임을 입력해주세요"))
			.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	@DisplayName("아카이브 생성 실패 - 글자 제한")
	void testCreateArchiveFail2() throws Exception {
		// Given
		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("123456789", Boundary.ALL);
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(post("/archive").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("이름은 8자 이하로 작성해주세요"))
			.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	@DisplayName("아카이브 생성 실패 - boundary")
	void testCreateArchiveFail3() throws Exception {
		// Given
		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("123456789", null);
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(post("/archive").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("공개범위를 입력해주세요"))
			.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	@DisplayName("아카이브 이름 변경 성공 테스트")
	void testChangeArchiveNameSuccessfully() throws Exception {
		// Given
		ArchiveNameRequestDto requestDto = new ArchiveNameRequestDto("New Name");
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(patch("/archive/{archiveId}/name", 1L).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("아카이브 이름 변경 성공"))
			.andExpect(jsonPath("$.code").value(2001));
	}

	@Test
	@DisplayName("아카이브 이름 변경 실패 - notBlank")
	void testChangeArchiveNameFail1() throws Exception {
		// Given
		ArchiveNameRequestDto requestDto = new ArchiveNameRequestDto("");
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// When & Then
		mockMvc.perform(patch("/archive/{archiveId}/name", 1L).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("닉네임을 입력해주세요"))
			.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	@DisplayName("아카이브 이름 변경 실패 - size")
	void testChangeArchiveNameFail2() throws Exception {
		// Given
		ArchiveNameRequestDto requestDto = new ArchiveNameRequestDto("New Name1111");
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(patch("/archive/{archiveId}/name", 1L).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("이름은 8자 이하로 작성해주세요"))
			.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	@DisplayName("아카이브 공개범위 변경 성공 테스트")
	void testChangeArchiveBoundarySuccessfully() throws Exception {
		// Given
		ArchiveBoundaryRequestDto requestDto = new ArchiveBoundaryRequestDto(Boundary.FOLLOW);
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(patch("/archive/{archiveId}/boundary", 1L).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("아카이브 공개범위 변경 성공"))
			.andExpect(jsonPath("$.code").value(2001));
	}

	@Test
	@DisplayName("아카이브 공개범위 변경 실패 테스트 - 유효성 검사 실패")
	void shouldFailToChangeArchiveBoundaryDueToValidation() throws Exception {
		// Given
		ArchiveBoundaryRequestDto requestDto = new ArchiveBoundaryRequestDto(null);
		String requestBody = objectMapper.writeValueAsString(requestDto);

		// Then
		mockMvc.perform(patch("/archive/{archiveId}/boundary", 1L).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("공개범위를 입력해주세요"))
			.andExpect(jsonPath("$.code").value(400));

	}
}