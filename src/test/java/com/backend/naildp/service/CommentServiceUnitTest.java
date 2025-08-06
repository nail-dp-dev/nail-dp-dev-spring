package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
import com.backend.naildp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

	@InjectMocks
	CommentService commentService;

	@Mock
	PostRepository postRepository;

	@Mock
	CommentRepository commentRepository;

	@Mock
	PostAccessValidator postAccessValidator;

	@Mock
	NotificationManager notificationManager;

	@Mock
	UserRepository userRepository;

	@Test
	void 임시저장_게시물에_댓글_등록_실패_테스트() {
		//given
		User user = createUserByNickname("nickname");
		Post tempSavedPost = createPost(user, true, Boundary.ALL);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(tempSavedPost));
		doThrow(new CustomException("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.NOT_FOUND))
			.when(postAccessValidator).isAvailablePost(eq(tempSavedPost), eq(user.getNickname()));

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("임시저장인 게시물에 댓글 등록시 예외 발생");

		//then
		Assertions.assertThrows(CustomException.class,
			() -> commentService.registerComment(1L, commentRegisterDto, "nickname"));
	}

	@DisplayName("비공개 게시물에 작성자가 아닌 사용자가 댓글 등록시 예외 발생")
	@Test
	void 비공개_게시물_댓글_등록_실패_테스트() {
		//given
		String userNickname = "nickname";
		User writer = createUserByNickname("writer");
		Post postVisibleToWriter = createPost(writer, false, Boundary.NONE);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(postVisibleToWriter));
		doThrow(new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY))
			.when(postAccessValidator).isAvailablePost(eq(postVisibleToWriter), eq(userNickname));

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("비공개 게시물에 댓글 등록시 예외 발생");

		//then
		Assertions.assertThrows(CustomException.class,
			() -> commentService.registerComment(1L, commentRegisterDto, userNickname));
	}

	@DisplayName("비공개 게시물에 작성자가 댓글 등록 테스트")
	@Test
	void registerCommentInPrivatePostByWriter() {
		//given
		String writerNickname = "writer";
		User writer = createUserByNickname(writerNickname);
		Post privatePost = createPost(writer, false, Boundary.NONE);
		Comment comment = new Comment(writer, privatePost, "댓글 등록");

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.ofNullable(privatePost));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		when(userRepository.findByNickname(eq(writerNickname))).thenReturn(Optional.ofNullable(writer));
		when(commentRepository.save(any(Comment.class))).thenReturn(comment);

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("댓글");
		commentService.registerComment(1L, commentRegisterDto, writerNickname);

		//then
		verify(postRepository).findPostAndUser(anyLong());
		verify(userRepository).findByNickname(eq(writerNickname));
		verify(commentRepository).save(any(Comment.class));
	}

	@DisplayName("팔로우 공개 게시물에 팔로워가 아닌 사용자가 댓글 등록시 예외 발생")
	@Test
	void registerCommentExceptionByNotFollower() {
		//given
		User notFollowedUser = createUserByNickname("notFollowedUser");
		User writer = createUserByNickname("writer");
		Post postOpenedForFollower = createPost(writer, false, Boundary.FOLLOW);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(postOpenedForFollower));
		doThrow(new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY))
			.when(postAccessValidator).isAvailablePost(eq(postOpenedForFollower), eq(notFollowedUser.getNickname()));

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("팔로우 공개 게시물에 댓글 등록시 예외 발생");

		//then
		Assertions.assertThrows(CustomException.class,
			() -> commentService.registerComment(1L, commentRegisterDto, notFollowedUser.getNickname()));
	}

	@DisplayName("팔로우 공개 게시물에 팔로워의 댓글 등록 테스트")
	@Test
	void registerCommentByFollower() {
		//given
		User followerUser = createUserByNickname("followerUser");
		User writer = createUserByNickname("writer");
		Post postOpenedForFollower = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(followerUser, postOpenedForFollower, "댓글 등록 성공");

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.ofNullable(postOpenedForFollower));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		when(userRepository.findByNickname(anyString())).thenReturn(Optional.ofNullable(followerUser));
		doNothing().when(notificationManager).handleCommentNotification(any(Comment.class), any(User.class));
		when(commentRepository.save(any(Comment.class))).thenReturn(comment);

		//when
		commentService.registerComment(1L, new CommentRegisterDto("댓글 등록 성공"), followerUser.getNickname());

		//then
		verify(postRepository).findPostAndUser(1L);
		verify(userRepository).findByNickname(followerUser.getNickname());
		verify(commentRepository).save(any(Comment.class));
	}

	@DisplayName("팔로우 공개 게시물에 작성자의 댓글 등록 테스트")
	@Test
	void registerCommentByWriter() {
		//given
		User writer = createUserByNickname("writer");
		Post postOpenedForFollower = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(writer, postOpenedForFollower, "댓글 등록 성공");

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.ofNullable(postOpenedForFollower));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		when(userRepository.findByNickname(anyString())).thenReturn(Optional.ofNullable(writer));
		when(commentRepository.save(any(Comment.class))).thenReturn(comment);

		//when
		commentService.registerComment(1L, new CommentRegisterDto("댓글 등록 성공"), writer.getNickname());

		//then
		verify(postRepository).findPostAndUser(1L);
		verify(userRepository).findByNickname(writer.getNickname());
		verify(commentRepository).save(any(Comment.class));
	}

	@DisplayName("전체 공개 게시물에 댓글 등록 테스트")
	@Test
	void registerCommentInPublicPostByNotFollower() {
		//given
		User commenter = createUserByNickname("commenter");
		User postWriter = createUserByNickname("postWriter");
		Post postOpenedForAllUser = createPost(postWriter, false, Boundary.ALL);
		Comment comment = new Comment(commenter, postOpenedForAllUser, "댓글 등록 성공");

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(postOpenedForAllUser));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		given(userRepository.findByNickname(eq(commenter.getNickname()))).willReturn(Optional.of(commenter));
		doNothing().when(notificationManager).handleCommentNotification(any(Comment.class), any(User.class));
		given(commentRepository.save(any())).willReturn(comment);

		//when
		CommentRegisterDto commentRegisterDto = new CommentRegisterDto("댓글 등록 성공");
		commentService.registerComment(1L, commentRegisterDto, commenter.getNickname());

		//then
		verify(postRepository).findPostAndUser(1L);
		verify(userRepository).findByNickname(commenter.getNickname());
		verify(commentRepository).save(any(Comment.class));
	}

	@Test
	void 다른_작성자가_댓글_수정시_예외_발생() {
		//given
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writerNickname");
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");
		CommentRegisterDto commentModifyDto = new CommentRegisterDto("modify comment");

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		when(commentRepository.findCommentAndUser(anyLong())).thenReturn(Optional.of(comment));

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
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writerNickname");
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");
		CommentRegisterDto commentModifyDto = new CommentRegisterDto("modify comment");

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
		when(commentRepository.findCommentAndUser(anyLong())).thenReturn(Optional.of(comment));

		//when
		commentService.modifyComment(1L, 1L, commentModifyDto, user.getNickname());

		//then
		verify(commentRepository).findCommentAndUser(anyLong());
	}

	@Test
	void 다른_작성자_댓글_삭제시_예외_발생() {
		//given
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writerNickname");
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
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
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writerNickname");
		Post post = createPost(writer, false, Boundary.FOLLOW);
		Comment comment = new Comment(user, post, "comment");

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(any(Post.class), anyString());
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
		CommentSummaryResponse response = commentService.findComments(postId, size, cursorId, "userNickname");
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertEquals(-1L, response.getCursorId());
		assertFalse(contents.hasNext());
		assertEquals(0, contents.getNumberOfElements());
	}

	private User createUserByNickname(String nickname) {
		return User.builder()
			.nickname(nickname)
			.phoneNumber("ph")
			.agreement(true)
			.role(UserRole.USER)
			.build();
	}

	private Post createPost(User user, boolean tempSave, Boundary boundary) {
		return Post.builder()
			.user(user)
			.postContent("content")
			.tempSave(tempSave)
			.boundary(boundary)
			.build();
	}

}