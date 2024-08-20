package com.backend.naildp.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.PostBoundaryRequest;
import com.backend.naildp.dto.post.PostInfoResponse;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PostController.class)
public class PostCreateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext context;

	@MockBean
	private PostService postService;

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
	@DisplayName("게시글 작성")
	public void testUploadPost() throws Exception {
		MockMultipartFile content = new MockMultipartFile("content", "", "application/json",
			"{\"postContent\":\"test content\",\"tempSave\":false,\"boundary\":\"ALL\",\"tags\":[{\"tagName\":\"tag1\"}]}".getBytes());
		MockMultipartFile photos = new MockMultipartFile("photos", "photo.jpg", "image/jpeg",
			"photo content".getBytes());

		willDoNothing().given(postService)
			.uploadPost(anyString(), any(PostRequestDto.class), any(List.class));

		mockMvc.perform(multipart("/posts")
				.file(content)
				.file(photos)
				.with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("게시글 작성이 완료되었습니다"))
			.andExpect(jsonPath("$.code").value(2001));
	}

	@Test
	@DisplayName("게시글 작성 - 파일 없음")
	public void testUploadPost_NoFiles() throws Exception {
		MockMultipartFile content = new MockMultipartFile("content", "", "application/json",
			"{\"postContent\":\"test content\",\"tempSave\":false,\"boundary\":\"ALL\",\"tags\":[{\"tagName\":\"tag1\"}]}".getBytes());

		willDoNothing().given(postService)
			.uploadPost(anyString(), any(PostRequestDto.class), any(List.class));

		mockMvc.perform(multipart("/posts")
				.file(content)
				.with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Required request part is missing"))
			.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	@DisplayName("게시글 작성 - 태그 없음")
	public void testUploadPost_NotTags() throws Exception {
		MockMultipartFile content = new MockMultipartFile("content", "", "application/json",
			"{\"postContent\":\"test content\",\"tempSave\":false,\"boundary\":\"ALL\"}".getBytes());
		MockMultipartFile photos = new MockMultipartFile("photos", "photo.jpg", "image/jpeg",
			"photo content".getBytes());

		mockMvc.perform(multipart("/posts")
				.file(content)
				.file(photos)
				.with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("태그를 추가해주세요"));
	}

	@Test
	@DisplayName("게시글 작성 - 태그 이름 없음")
	public void testUploadPost_NOtTagName() throws Exception {
		MockMultipartFile content = new MockMultipartFile("content", "", "application/json",
			"{\"postContent\":\"test content\",\"tempSave\":false,\"boundary\":\"ALL\",\"tags\":[{\"tagName\":\"\"}]}".getBytes());
		MockMultipartFile photos = new MockMultipartFile("photos", "photo.jpg", "image/jpeg",
			"photo content".getBytes());

		mockMvc.perform(multipart("/posts")
				.file(content)
				.file(photos)
				.with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("태그를 입력해주세요"));
	}

	@Test
	@DisplayName("게시글 수정")
	public void testEditPost() throws Exception {
		MockMultipartFile content = new MockMultipartFile("content", "", "application/json",
			"{\"postContent\":\"edited content\",\"tempSave\":false,\"boundary\":\"ALL\",\"tags\":[{\"tagName\":\"tag2\"}],\"deletedFileUrls\":[]}".getBytes());

		MockMultipartFile photos = new MockMultipartFile("photos", "photo.jpg", "image/jpeg",
			"photo content".getBytes());

		willDoNothing().given(postService).editPost(anyString(), any(PostRequestDto.class), any(List.class), anyLong());

		mockMvc.perform(multipart("/posts/{postId}", 1L)
				.file(content)
				.file(photos)
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				})
				.with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andDo(print()) // 요청 및 응답 내용을 콘솔에 출력
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("게시글 수정이 완료되었습니다"))
			.andExpect(jsonPath("$.code").value(2001));
	}

	@Test
	@DisplayName("게시글 수정 - content 없음")
	public void testEditPostNoContent() throws Exception {

		willDoNothing().given(postService).editPost(anyString(), any(PostRequestDto.class), any(List.class), anyLong());

		mockMvc.perform(multipart("/posts/{postId}", 1L)
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				})
				.with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Required request part is missing"));
	}

	@Test
	@DisplayName("수정 게시글 조회")
	public void testGetEditingPost() throws Exception {
		EditPostResponseDto editPostResponseDto = EditPostResponseDto.builder()
			.postContent("content")
			.boundary(Boundary.ALL)
			.photos(List.of())
			.tags(List.of("tag1"))
			.tempSave(false)
			.build();

		given(postService.getEditingPost("testUser", 1L)).willReturn(editPostResponseDto);

		mockMvc.perform(get("/posts/edit/{postId}", 1L).with(csrf())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.postContent").value("content"))
			.andExpect(jsonPath("$.data.boundary").value("ALL"))
			.andExpect(jsonPath("$.data.tags[0]").value("tag1"))
			.andExpect(jsonPath("$.data.tempSave").value(false))
			.andExpect(jsonPath("$.message").value("수정 게시글 조회 완료"))
			.andExpect(jsonPath("$.code").value(2000));
	}

	@DisplayName("특정 게시글 조회 API 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void readPostDetails() throws Exception {
		//given
		PostInfoResponse response = PostInfoResponse.builder()
			.nickname("testUser")
			.profileUrl("url")
			.followingStatus(false)
			.followerCount(0L)
			.files(List.of(new PostInfoResponse.FileInfoResponse("photo.jpg", true, false)))
			.postContent("content")
			.likeCount(0L)
			.commentCount(0L)
			.sharedCount(0L)
			.tags(List.of("tag1", "tag2"))
			.build();

		//when
		when(postService.postInfo(anyString(), anyLong())).thenReturn(response);

		//then
		mockMvc.perform(get("/posts/{postId}", 1L)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("특정 게시물 상세정보 조회"))
			.andExpect(jsonPath("$.code").value(2000))
			.andDo(print());
	}

	@DisplayName("특정 게시글 조회 API 예외 - 게시물 접근 권한이 없을 때")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void readPostDetailsException() throws Exception {
		//given
		CustomException exception = new CustomException("게시물을 읽을 수 없습니다.", ErrorCode.NOT_FOUND);

		//when
		when(postService.postInfo(anyString(), anyLong())).thenThrow(exception);

		//then
		mockMvc.perform(get("/posts/{postId}", 1L)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(exception.getMessage()))
			.andExpect(jsonPath("$.code").value(exception.getErrorCode().getErrorCode()))
			.andDo(print());
	}

	@DisplayName("게시물 공개 범위 변경 API 예외 - 요청자와 게시물 작성자가 다를 때")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void changePostBoundaryExceptionWithOtherUser() throws Exception {
		//given
		Long postId = 1L;
		PostBoundaryRequest postBoundaryRequest = new PostBoundaryRequest(Boundary.FOLLOW);
		String wrongUserNickname = "wrongNickname";

		String request = objectMapper.writeValueAsString(postBoundaryRequest);

		CustomException exception = new CustomException("게시글 범위 설정은 작성자만 할 수 있습니다.", ErrorCode.USER_MISMATCH);
		doThrow(exception).when(postService).changeBoundary(anyLong(), any(PostBoundaryRequest.class), anyString());

		//when
		ResultActions resultActions = mockMvc.perform(
			patch("/posts/{postId}/closer", postId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(request)
				.with(csrf()));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(exception.getMessage()))
			.andExpect(jsonPath("$.code").value(exception.getErrorCode().getErrorCode()))
			.andDo(print());
	}

	@DisplayName("게시물 공개 범위 변경")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void changePostBoundaryApiTest() throws Exception {
		//given
		Long postId = 1L;
		PostBoundaryRequest postBoundaryRequest = new PostBoundaryRequest(Boundary.FOLLOW);
		String wrongUserNickname = "wrongNickname";

		String request = objectMapper.writeValueAsString(postBoundaryRequest);
		ApiResponse<Object> apiResponse = ApiResponse.successResponse(null, "게시글 공개범위 설정 완료", 2000);

		doNothing().when(postService).changeBoundary(anyLong(), any(PostBoundaryRequest.class), anyString());

		//when
		ResultActions resultActions = mockMvc.perform(
			patch("/posts/{postId}/closer", postId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(request)
				.with(csrf()));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andDo(print());
	}
}

