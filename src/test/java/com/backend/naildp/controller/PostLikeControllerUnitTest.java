package com.backend.naildp.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.service.PostLikeService;

@WebMvcTest(PostLikeController.class)
class PostLikeControllerUnitTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	PostLikeService postLikeService;

	@DisplayName("게시물 좋아요 API 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likePostApiTest() throws Exception {
		Long resultId = 10L;
		ApiResponse<Object> apiResponse = ApiResponse.successResponse(null, "좋아요 목록에 추가", 2001);

		when(postLikeService.likeByPostId(anyLong(), anyString())).thenReturn(resultId);

		mvc.perform(post("/posts/{postId}/likes", 3L).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}

	@DisplayName("게시물 좋아요 API 예외 테스트 - 게시물 조회 예외")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likePostApiExceptionByPost() throws Exception {
		Long resultId = 10L;

		ApiResponse<?> apiResponse = ApiResponse.of(ErrorCode.NOT_FOUND);
		apiResponse.setMessage("해당 포스트를 조회할 수 없습니다.");

		when(postLikeService.likeByPostId(anyLong(), anyString()))
			.thenThrow(new CustomException("해당 포스트를 조회할 수 없습니다.", ErrorCode.NOT_FOUND));

		mvc.perform(post("/posts/{postId}/likes", 3L).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andDo(print());
	}

	@DisplayName("게시물 좋아요 취소 API 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void cancelPostLikeApiTest() throws Exception {
		Long resultId = 10L;
		ApiResponse<Object> apiResponse = ApiResponse.successResponse(null, "좋아요 취소", 2000);

		when(postLikeService.likeByPostId(anyLong(), anyString())).thenReturn(resultId);

		mvc.perform(delete("/posts/{postId}/likes", 3L).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}

}