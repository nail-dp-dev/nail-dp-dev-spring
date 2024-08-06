package com.backend.naildp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerUnitTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	CommentService commentService;

	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void 잘못된_형식으로_댓글_작성시_예외_발생() throws Exception {
		//given
		String message = "댓글을 입력해주세요";
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(message);

		String nullRequest = objectMapper.writeValueAsString(new CommentRegisterDto());
		String emptyStringRequest = objectMapper.writeValueAsString(new CommentRegisterDto(""));
		String blankStringRequest = objectMapper.writeValueAsString(new CommentRegisterDto("  "));

		//when
		ResultActions nullResultActions = mvc.perform(
			post("/posts/{postId}/comment", 1L)
				.content(nullRequest)
				.contentType(MediaType.APPLICATION_JSON));
		ResultActions emptyStringResultActions = mvc.perform(
			post("/posts/{postId}/comment", 1L)
				.content(emptyStringRequest)
				.contentType(MediaType.APPLICATION_JSON));
		ResultActions blankResultActions = mvc.perform(
			post("/posts/{postId}/comment", 1L)
				.content(blankStringRequest)
				.contentType(MediaType.APPLICATION_JSON));

		//then
		nullResultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
		emptyStringResultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
		blankResultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}

	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void 댓글_작성_API_테스트() throws Exception {
		//given
		Long registeredCommentId = 1L;
		ApiResponse<Long> apiResponse = ApiResponse.successResponse(registeredCommentId, "댓글 등록 성공", 2001);

		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("댓글");
		String request = objectMapper.writeValueAsString(commentRegisterDto);

		//when
		ResultActions resultActions = mvc.perform(
			post("/posts/{postId}/comment", 1L)
				.content(request)
				.contentType(MediaType.APPLICATION_JSON));

		//then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}

	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void 잘못된_형식으로_댓글_수정시_예외_발생() throws Exception {
		String message = "댓글을 입력해주세요";
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(message);

		String nullRequest = objectMapper.writeValueAsString(new CommentRegisterDto());
		String emptyStringRequest = objectMapper.writeValueAsString(new CommentRegisterDto(""));
		String blankStringRequest = objectMapper.writeValueAsString(new CommentRegisterDto("  "));

		//when
		ResultActions nullResultActions = mvc.perform(
			patch("/posts/{postId}/comment/{commentId}", 1L, 2L)
				.content(nullRequest)
				.contentType(MediaType.APPLICATION_JSON));
		ResultActions emptyStringResultActions = mvc.perform(
			patch("/posts/{postId}/comment/{commentId}", 1L, 2L)
				.content(emptyStringRequest)
				.contentType(MediaType.APPLICATION_JSON));
		ResultActions blankResultActions = mvc.perform(
			patch("/posts/{postId}/comment/{commentId}", 1L, 2L)
				.content(blankStringRequest)
				.contentType(MediaType.APPLICATION_JSON));

		//then
		nullResultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
		emptyStringResultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
		blankResultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}

	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void 댓글_수정_API_테스트() throws Exception {
		//given
		Long modifiedCommentId = 1L;
		ApiResponse<Long> apiResponse = ApiResponse.successResponse(modifiedCommentId, "댓글 수정 성공", 2000);

		CommentRegisterDto commentModifyDto = new CommentRegisterDto("수정 댓글");
		String request = objectMapper.writeValueAsString(commentModifyDto);

		//when
		ResultActions resultActions = mvc.perform(
			patch("/posts/{postId}/comment/{commentId}", 1L, 2L)
				.content(request)
				.contentType(MediaType.APPLICATION_JSON));

		//then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}
}