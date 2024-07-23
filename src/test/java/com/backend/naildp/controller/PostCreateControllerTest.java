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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TagRequestDto;
import com.backend.naildp.dto.post.TempPostRequestDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.PostService;

@WebMvcTest(PostController.class)
public class PostCreateControllerTest {

	@Autowired
	private MockMvc mockMvc;

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

		user = new User("testUser", "010-1234-5678", 0L, UserRole.USER);

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

	@Test
	@DisplayName("게시글 임시저장")
	public void testTempSavePost() throws Exception {
		TempPostRequestDto tempPostRequestDto = new TempPostRequestDto("temp content", true, Boundary.ALL,
			List.of(new TagRequestDto("tag1")), null);
		List<MultipartFile> files = List.of(); // Assuming empty list for the sake of example

		mockMvc.perform(post("/posts/temp").with(csrf())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.param("content", "temp content")
				.param("tempSave", "true")
				.param("boundary", "ALL")
				.param("tags", "tag1")
				.param("photos", files.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("게시글 임시저장이 완료되었습니다"))
			.andExpect(jsonPath("$.code").value(2001));
	}
}

