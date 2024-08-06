package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
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