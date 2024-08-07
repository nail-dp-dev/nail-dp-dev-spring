package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.comment.CommentInfoResponse;
import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.dto.comment.CommentSummaryResponse;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.CommentRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

	@InjectMocks
	CommentService commentService;

	@Mock
	PostRepository postRepository;

	@Mock
	CommentRepository commentRepository;

	@Mock
	FollowRepository followRepository;

	@Test
	void 임시저장_게시물에_댓글_등록_실패_테스트() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		Post post = createPost(user, true, Boundary.ALL);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("임시저장인 게시물에 댓글 등록시 예외 발생");

		//then
		Assertions.assertThrows(CustomException.class,
			() -> commentService.registerComment(1L, commentRegisterDto, "nickname"));
	}

	@Test
	void 비공개_게시물_댓글_등록_실패_테스트() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		Post post = createPost(user, false, Boundary.NONE);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("비공개 게시물에 댓글 등록시 예외 발생");

		//then
		Assertions.assertThrows(CustomException.class,
			() -> commentService.registerComment(1L, commentRegisterDto, "nickname"));
	}

	@Test
	void 팔로우_게시물_댓글_등록_실패_테스트() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		User writer = new User(new LoginRequestDto("writerNickname", "writerPhoneNumber", true), UserRole.USER);
		Post post = createPost(writer, false, Boundary.FOLLOW);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		given(followRepository.existsByFollowerNicknameAndFollowing(eq(user.getNickname()), eq(writer)))
			.willReturn(false);

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("팔로우 공개 게시물에 댓글 등록시 예외 발생");

		//then
		Assertions.assertThrows(CustomException.class,
			() -> commentService.registerComment(1L, commentRegisterDto, user.getNickname()));
	}

	@Test
	void 댓글_등록_테스트() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		User writer = new User(new LoginRequestDto("writerNickname", "writerPhoneNumber", true), UserRole.USER);
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "댓글 등록 성공");

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		given(followRepository.existsByFollowerNicknameAndFollowing(eq(user.getNickname()), eq(writer)))
			.willReturn(true);
		given(commentRepository.save(any())).willReturn(comment);

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("댓글 등록 성공");
		commentService.registerComment(1L, commentRegisterDto, user.getNickname());

		//then
		verify(postRepository).findPostAndUser(post.getId());
		verify(followRepository).existsByFollowerNicknameAndFollowing(user.getNickname(), writer);
		verify(commentRepository).save(any(Comment.class));
	}

	@Test
	void 다른_작성자가_댓글_수정시_예외_발생() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		User writer = new User(new LoginRequestDto("writerNickname", "writerPhoneNumber", true), UserRole.USER);
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");
		CommentRegisterDto commentModifyDto = new CommentRegisterDto("modify comment");

		when(commentRepository.findCommentAndPostAndUser(anyLong())).thenReturn(Optional.of(comment));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentService.modifyComment(1L, 1L, commentModifyDto, "otherNickname"));

		//then
		assertEquals("댓글은 작성자만 수정할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.COMMENT_AUTHORITY, exception.getErrorCode());
	}

	@Test
	void 댓글_수정_성공_테스트() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		User writer = new User(new LoginRequestDto("writerNickname", "writerPhoneNumber", true), UserRole.USER);
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");
		CommentRegisterDto commentModifyDto = new CommentRegisterDto("modify comment");

		when(commentRepository.findCommentAndPostAndUser(anyLong())).thenReturn(Optional.of(comment));

		//when
		commentService.modifyComment(1L, 1L, commentModifyDto, user.getNickname());

		//then
		verify(commentRepository).findCommentAndPostAndUser(anyLong());
	}

	@Test
	void 다른_작성자_댓글_삭제시_예외_발생() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		User writer = new User(new LoginRequestDto("writerNickname", "writerPhoneNumber", true), UserRole.USER);
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");

		when(commentRepository.findCommentAndPostAndUser(anyLong())).thenReturn(Optional.of(comment));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentService.deleteComment(1L, 1L, "otherNickname"));

		//then
		assertEquals("댓글은 작성자만 삭제할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.COMMENT_AUTHORITY, exception.getErrorCode());
	}

	@Test
	void 댓글_삭제_성공_테스트() {
		//given
		User user = new User(new LoginRequestDto("nickname", "phoneNumber", true), UserRole.USER);
		User writer = new User(new LoginRequestDto("writerNickname", "writerPhoneNumber", true), UserRole.USER);
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");

		when(commentRepository.findCommentAndPostAndUser(anyLong())).thenReturn(Optional.of(comment));
		doNothing().when(commentRepository).delete(any(Comment.class));

		//when
		commentService.deleteComment(1L, 1L, user.getNickname());

		//then
		verify(commentRepository).findCommentAndPostAndUser(anyLong());
		verify(commentRepository).delete(any(Comment.class));
	}

	@Test
	void 댓글이_0개일때_조회_테스트() {
		//given
		int size = 20;
		long postId = 1L;
		long cursorId = -1L;

		List<Comment> emptyComments = new ArrayList<>();
		PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "likeCount", "createdDate"));
		Slice<Comment> emptyCommentSlice = new SliceImpl<>(emptyComments, pageRequest, false);

		when(commentRepository.findCommentsByPostId(anyLong(), any(PageRequest.class))).thenReturn(emptyCommentSlice);

		//when
		CommentSummaryResponse response = commentService.findComments(postId, size, cursorId);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertEquals(-1L, response.getCursorId());
		assertFalse(contents.hasNext());
		assertEquals(0, contents.getNumberOfElements());
	}

	private Post createPost(User user, boolean tempSave, Boundary boundary) {
		return Post.builder()
			.id(1L)
			.user(user)
			.postContent("content")
			.tempSave(tempSave)
			.boundary(boundary)
			.build();
	}

}