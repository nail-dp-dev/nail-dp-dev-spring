package com.backend.naildp.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.backend.naildp.dto.comment.CommentInfoResponse;
import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.dto.comment.CommentSummaryResponse;
import com.backend.naildp.entity.Comment;
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

	@DisplayName("댓글이 없는 게시물에서 댓글 조회 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void findNoComments() throws Exception {
		//given
		CommentSummaryResponse response = new CommentSummaryResponse(-1L, new SliceImpl<>(new ArrayList<>()));
		ApiResponse<CommentSummaryResponse> apiResponse = ApiResponse.successResponse(response, "댓글 조회 성공", 2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		when(commentService.findComments(anyLong(), anyInt(), eq(-1L))).thenReturn(response);

		//when
		ResultActions resultActions = mvc.perform(get("/posts/{postId}/comment", 1L));

		//then
		resultActions
			.andExpect(status().isOk())
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andDo(print());
	}

	@DisplayName("댓글 조회 API 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void findCommentsTest() throws Exception {
		//given
		List<CommentInfoResponse> commentInfoResponseList = new ArrayList<>();
		for (long i = 1; i <= 2; i++) {
			CommentInfoResponse commentInfoResponse = CommentInfoResponse.builder()
				.commentId(i)
				.commentContent("comment")
				.profileUrl("profileUrl")
				.commentUserNickname("nickname")
				.commentDate(LocalDateTime.now())
				.likeCount(i)
				.build();
			commentInfoResponseList.add(commentInfoResponse);
		}
		PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "likeCount", "createdDate"));
		Slice<CommentInfoResponse> commentInfoResponseSlice = new SliceImpl<>(commentInfoResponseList, pageRequest,
			true);

		CommentSummaryResponse response = new CommentSummaryResponse(1L, commentInfoResponseSlice);
		ApiResponse<CommentSummaryResponse> apiResponse = ApiResponse.successResponse(response, "댓글 조회 성공", 2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		when(commentService.findComments(anyLong(), anyInt(), eq(-1L))).thenReturn(response);

		//when
		ResultActions resultActions = mvc.perform(get("/posts/{postId}/comment", 1L).param("size", "2"));

		//then
		resultActions
			.andExpect(status().isOk())
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andDo(print());
	}
}