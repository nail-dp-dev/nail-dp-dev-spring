package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.CommentLikeRepository;
import com.backend.naildp.repository.CommentRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceUnitTest {

	@InjectMocks
	CommentLikeService commentLikeService;

	@Mock
	PostRepository postRepository;
	@Mock
	PostAccessValidator postAccessValidator;
	@Mock
	UserRepository userRepository;
	@Mock
	CommentRepository commentRepository;
	@Mock
	CommentLikeRepository commentLikeRepository;
	@Mock
	NotificationManager notificationManager;

	@DisplayName("임시저장 게시물에 접근할 때 예외 테스트")
	@Test
	void accessToTempSavePostException() {
		User user = createUser("nickname");
		Post tempSavedPost = createPost(user, true, Boundary.ALL);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(tempSavedPost));
		doThrow(new CustomException("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.NOT_FOUND))
			.when(postAccessValidator).isAvailablePost(eq(tempSavedPost), eq(user.getNickname()));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentLikeService.likeComment(1L, 2L, "nickname"));

		//then
		assertEquals("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", exception.getMessage());
		assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
	}

	@DisplayName("비공개 게시물에 작성자가 아닌 사용자가 접근할 때 예외 테스트")
	@Test
	void accessToPrivatePostException() {
		String wrongNickname = "wrongNickname";
		User user = createUser("nickname");
		Post postVisibleToWriter = createPost(user, false, Boundary.NONE);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(postVisibleToWriter));
		doThrow(new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY))
			.when(postAccessValidator).isAvailablePost(eq(postVisibleToWriter), eq(wrongNickname));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentLikeService.likeComment(1L, 2L, wrongNickname));

		//then
		assertEquals("비공개 게시물은 작성자만 접근할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.INVALID_BOUNDARY, exception.getErrorCode());
	}

	@DisplayName("팔로우 공개 게시물에 팔로우 하지 않은 사용자가 접근할 때 예외 테스트")
	@Test
	void accessToFollowPostException() {
		String notFollowNickname = "notFollowNickname";
		User user = createUser("nickname");
		Post postVisibleToFollower = createPost(user, false, Boundary.FOLLOW);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(postVisibleToFollower));
		doThrow(new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY))
			.when(postAccessValidator).isAvailablePost(eq(postVisibleToFollower), eq(notFollowNickname));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentLikeService.likeComment(1L, 2L, notFollowNickname));

		//then
		assertEquals("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.INVALID_BOUNDARY, exception.getErrorCode());
	}

	@DisplayName("공개범위 별 게시물에 작성자가 댓글 좋아요 테스트")
	@ParameterizedTest
	@EnumSource(Boundary.class)
	void likeCommentBy(Boundary boundary) {
		User writer = createUser("nickname");
		User commenter = createUser("commenter");
		Post post = createPost(writer, false, boundary);
		Comment comment = new Comment(commenter, post, "comment");
		CommentLike commentLike = new CommentLike(writer, comment);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
		when(userRepository.findByNickname(anyString())).thenReturn(Optional.of(writer));
		when(commentLikeRepository.saveAndFlush(any(CommentLike.class))).thenReturn(commentLike);
		doNothing().when(notificationManager)
			.handleNotificationFromCommentLike(any(Comment.class), any(User.class), any(CommentLike.class));

		//when
		Long commentLikeId = commentLikeService.likeComment(1L, 2L, writer.getNickname());

		//then
		assertThat(commentLikeId).isNull();
	}

	private User createUser(String nickname) {
		return User.builder().nickname(nickname).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
	}

	private Post createPost(User user, boolean tempSave, Boundary boundary) {
		return Post.builder().user(user).postContent("content").tempSave(tempSave).boundary(boundary).build();
	}
}