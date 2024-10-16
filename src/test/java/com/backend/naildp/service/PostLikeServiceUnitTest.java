package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceUnitTest {

	@InjectMocks
	PostLikeService postLikeService;

	@Mock
	UserRepository userRepository;

	@Mock
	PostRepository postRepository;

	@Mock
	PostLikeRepository postLikeRepository;

	@Mock
	FollowRepository followRepository;

	@Test
	@DisplayName("게시물 Id 로 좋아요 저장 테스트")
	void savePostLike() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, "phoneNumber", true);
		User user = new User(loginRequestDto, UserRole.USER);
		Post post = new Post(user, "content", 0L, Boundary.ALL, false);
		PostLike postLike = new PostLike(user, post);

		given(userRepository.findUserByNickname(anyString())).willReturn(Optional.of(user));
		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(postLikeRepository.save(any(PostLike.class))).willReturn(postLike);

		//when
		Long savedPostLikeId = postLikeService.likeByPostId(postId, nickname);

		//then
		verify(userRepository, times(1)).findUserByNickname(nickname);
		verify(postRepository, times(1)).findById(postId);
		verify(postLikeRepository, times(1)).save(any(PostLike.class));
	}

	@Test
	@DisplayName("이미 게시물을 좋아요 했을 때 게시물 좋아요 요청시 저장된 PostLike 반환")
	void saveAlreadyLikedPost() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, "phoneNumber", true);
		User user = new User(loginRequestDto, UserRole.USER);
		Post post = new Post(user, "content", 0L, Boundary.ALL, false);
		PostLike postLike = new PostLike(user, post);

		given(userRepository.findUserByNickname(anyString())).willReturn(Optional.of(user));
		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(postLikeRepository.findPostLikeByUserNicknameAndPostId(eq(nickname), eq(postId)))
			.willReturn(Optional.of(postLike));

		//when
		postLikeService.likeByPostId(postId, nickname);

		//then
		verify(userRepository).findUserByNickname(nickname);
		verify(postRepository).findById(postId);
		verify(postLikeRepository, never()).save(any(PostLike.class));
	}

	@DisplayName("게시물 좋아요 취소 테스트")
	@Test
	void cancelPostLikeByPostId() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, "phoneNumber", true);
		User user = new User(loginRequestDto, UserRole.USER);
		Post post = new Post(user, "content", 0L, Boundary.ALL, false);
		PostLike postLike = new PostLike(user, post);

		given(postLikeRepository.findPostLikeByUserNicknameAndPostId(anyString(), anyLong()))
			.willReturn(Optional.of(postLike));

		//when
		postLikeService.unlikeByPostId(postId, nickname);

		//then
		verify(postLikeRepository).findPostLikeByUserNicknameAndPostId(anyString(), anyLong());
		verify(postLikeRepository).deletePostLikeById(postLike.getId());
	}

	@DisplayName("게시물 좋아요 취소 예외 테스트")
	@Test
	void cancelPostLikeException() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		String wrongNickname = "wrong";
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, "phoneNumber", true);
		User user = new User(loginRequestDto, UserRole.USER);
		Post post = new Post(user, "content", 0L, Boundary.ALL, false);
		PostLike postLike = new PostLike(user, post);

		given(postLikeRepository.findPostLikeByUserNicknameAndPostId(anyString(), anyLong()))
			.willThrow(new CustomException("해당 게시물은 존재하지 않습니다.", ErrorCode.NOT_FOUND));

		//then
		assertThatThrownBy(() -> postLikeService.unlikeByPostId(postId, nickname))
			.isInstanceOf(CustomException.class);
	}

	@DisplayName("임시 저장한 게시물 좋아요 숫자 조회시 예외 발생")
	@Test
	void countPostLikeExceptionWithTempSavePost() {
		//given
		User user = createUser("nickname");
		Post tempSavePost = createPost(user, true, Boundary.ALL);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(tempSavePost));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> postLikeService.countPostLike(1L, user.getNickname()));

		//then
		assertEquals("임시저장한 게시물은 좋아요를 볼 수 없습니다.", exception.getMessage());
		assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
	}

	@DisplayName("작성자가 아닌 사용자가 비공개 게시물을 좋아요 숫자 조회시 예외 발생")
	@Test
	void countPostLikeExceptionWithPrivatePostAndOtherUser() {
		//given
		String wrongNickname = "wrongNickname";
		User user = createUser("nickname");
		Post privatePost = createPost(user, false, Boundary.NONE);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(privatePost));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> postLikeService.countPostLike(1L, wrongNickname));

		//then
		assertEquals("비공개 게시물은 좋아요를 볼 수 없습니다.", exception.getMessage());
		assertEquals(ErrorCode.INVALID_BOUNDARY, exception.getErrorCode());
	}

	@DisplayName("팔로워가 아닌 일반 사용자가 팔로우 공개 게시물 좋아요 숫자 조회시 예외 발생")
	@Test
	void countPostLikeExceptionWithFollowOpenedPostAndNotFollower() {
		//given
		String notFollowerNickname = "notFollowerNickname";
		User user = createUser("nickname");
		Post followPost = createPost(user, false, Boundary.FOLLOW);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(followPost));
		when(followRepository.existsByFollowerNicknameAndFollowing(eq(notFollowerNickname), eq(user))).thenReturn(
			false);

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> postLikeService.countPostLike(1L, notFollowerNickname));

		//then
		assertEquals("팔로워 공개 게시물입니다. 팔로워와 작성자만 좋아요할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.INVALID_BOUNDARY, exception.getErrorCode());
	}

	@DisplayName("게시물 작성자가 팔로우 공개 게시물 좋아요 숫자 조회 테스트")
	@Test
	void countPostLikeExceptionWithFollowOpenedPostAndWriter() {
		//given
		String postWriterNickname = "writer";
		User postWriter = createUser(postWriterNickname);
		Post followPost = createPost(postWriter, false, Boundary.FOLLOW);
		PostLike postLike = new PostLike(postWriter, followPost);
		followPost.addPostLike(postLike);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(followPost));
		when(followRepository.existsByFollowerNicknameAndFollowing(eq(postWriterNickname), eq(postWriter))).thenReturn(false);

		//when
		PostLikeCountResponse response = postLikeService.countPostLike(1L, postWriterNickname);

		//then
		assertThat(response.getLikeCount()).isEqualTo(1);
	}

	@DisplayName("전체 공개 게시물 좋아요 숫자 조회 테스트")
	@Test
	void countLikeAboutPublicPost() {
		//given
		User user = createUser("nickname");
		Post publicPost = createPost(user, false, Boundary.ALL);
		long likeCnt = 30;
		for (long i = 0; i < likeCnt; i++) {
			PostLike postLike = new PostLike(user, publicPost);
			publicPost.addPostLike(postLike);
		}

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(publicPost));

		//when
		PostLikeCountResponse response = postLikeService.countPostLike(1L, user.getNickname());

		//then
		assertThat(response.getLikeCount()).isEqualTo(likeCnt);
	}

	@DisplayName("팔로우 공개 게시물 좋아요 숫자 조회 테스트")
	@Test
	void countPostLike() {
		//given
		User user = createUser("nickname");
		Post postOpenedForFollower = createPost(user, false, Boundary.FOLLOW);
		long likeCnt = 30;
		for (long i = 0; i < likeCnt; i++) {
			PostLike postLike = new PostLike(user, postOpenedForFollower);
			postOpenedForFollower.addPostLike(postLike);
		}

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(postOpenedForFollower));
		when(followRepository.existsByFollowerNicknameAndFollowing(anyString(), any(User.class))).thenReturn(true);

		//when
		PostLikeCountResponse response = postLikeService.countPostLike(1L, user.getNickname());

		//then
		assertThat(response.getLikeCount()).isEqualTo(likeCnt);
	}

	private User createUser(String nickname) {
		return User.builder().nickname(nickname).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
	}

	private Post createPost(User user, boolean tempSave, Boundary boundary) {
		return Post.builder().user(user).postContent("content").tempSave(tempSave).boundary(boundary).build();
	}
}