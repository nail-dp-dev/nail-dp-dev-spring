package com.backend.naildp.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.service.CommentLikeService;

@SpringBootTest
@AutoConfigureMockMvc
class CommentLikeControllerUnitTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	CommentLikeService commentLikeService;

	@DisplayName("댓글 좋아요 등록 예외 - 임시저장된 게시물에 접근했을 때")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likeCountExceptionByTempSavedPost() throws Exception {
		//given
		when(commentLikeService.likeComment(anyLong(), anyLong(), anyString()))
			.thenThrow(new CustomException("임시저장한 게시물에 댓글을 등록할 수 없습니다.", ErrorCode.NOT_FOUND));

		//when
		ResultActions resultActions = mvc.perform(post("/posts/{postId}/comment/{commentId}/like", 1L, 2L));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("임시저장한 게시물에 댓글을 등록할 수 없습니다."))
			.andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getErrorCode()))
			.andDo(print());
	}

	@DisplayName("댓글 좋아요 등록 예외 - 비공개 게시물에 작성자가 아닌 사용자가 접근했을 때")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likeCountExceptionByPrivatePostAndUserNotWriter() throws Exception {
		//given
		when(commentLikeService.likeComment(anyLong(), anyLong(), anyString()))
			.thenThrow(new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY));

		//when
		ResultActions resultActions = mvc.perform(post("/posts/{postId}/comment/{commentId}/like", 1L, 2L));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("비공개 게시물은 작성자만 접근할 수 있습니다."))
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_BOUNDARY.getErrorCode()))
			.andDo(print());
	}

	@DisplayName("댓글 좋아요 등록 예외 - 팔로우 공개 게시물에 팔로워와 작성자가 아닌 사용자가 접근했을 때")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likeCountExceptionByFollowPostAndUserNotFollower() throws Exception {
		//given
		when(commentLikeService.likeComment(anyLong(), anyLong(), anyString()))
			.thenThrow(new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY));

		//when
		ResultActions resultActions = mvc.perform(post("/posts/{postId}/comment/{commentId}/like", 1L, 2L));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다."))
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_BOUNDARY.getErrorCode()))
			.andDo(print());
	}

	@DisplayName("댓글 좋아요 등록 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likeCountTest() throws Exception {
		//given
		Long commentLikeId = 1L;
		when(commentLikeService.likeComment(anyLong(), anyLong(), anyString()))
			.thenReturn(commentLikeId);

		//when
		ResultActions resultActions = mvc.perform(post("/posts/{postId}/comment/{commentId}/like", 1L, 2L));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글 좋아요 성공"))
			.andExpect(jsonPath("$.code").value(2001))
			.andExpect(jsonPath("$.data").value(commentLikeId))
			.andDo(print());
	}

	@DisplayName("댓글 좋아요 취소 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void cancelCommentLike() throws Exception {
		//given
		doNothing().when(commentLikeService).cancelCommentLike(anyLong(), anyLong(), anyString());

		//when
		ResultActions resultActions = mvc.perform(delete("/posts/{postId}/comment/{commentId}/like", 1L, 2L));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글 좋아요 취소 성공"))
			.andExpect(jsonPath("$.code").value(2001))
			.andDo(print());
	}

	@DisplayName("댓글 좋아요 개수 조회 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void countCommentLike() throws Exception {
		//given
		PostLikeCountResponse likeCountResponse = new PostLikeCountResponse(10);
		when(commentLikeService.countCommentLikes(anyLong(), anyLong(), anyString())).thenReturn(likeCountResponse);

		//when
		ResultActions resultActions = mvc.perform(get("/posts/{postId}/comment/{commentId}/like", 1L, 2L));

		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글 좋아요 개수 조회 성공"))
			.andExpect(jsonPath("$.code").value(2000))
			.andExpect(jsonPath("$.data.likeCount").value(10))
			.andDo(print());
	}
}