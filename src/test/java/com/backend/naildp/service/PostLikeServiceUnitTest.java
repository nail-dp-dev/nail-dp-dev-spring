package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
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
import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
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
	PostAccessValidator postAccessValidator;

	@Mock
	NotificationManager notificationManager;

	@Test
	@DisplayName("게시물 Id 로 좋아요 저장 테스트")
	void savePostLike() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		User user = createUser(nickname);
		Post post = createPost(user, Boundary.ALL);
		PostLike postLike = new PostLike(user, post);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(eq(post), eq(nickname));

		given(userRepository.findUserByNickname(anyString())).willReturn(Optional.of(user));
		given(postLikeRepository.save(any(PostLike.class))).willReturn(postLike);
		doNothing().when(notificationManager)
			.handlePostLikeNotification(any(User.class), any(Post.class), any(PostLike.class));

		//when
		postLikeService.likeByPostId(postId, nickname);

		//then
		verify(userRepository).findUserByNickname(nickname);
		verify(postRepository).findPostAndUser(postId);
		verify(postLikeRepository).save(any(PostLike.class));
	}

	@Test
	@DisplayName("이미 게시물을 좋아요 했을 때 게시물 좋아요 요청시 저장된 PostLike 반환")
	void saveAlreadyLikedPost() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		User user = createUser(nickname);
		Post post = createPost(user, Boundary.ALL);
		PostLike postLike = new PostLike(user, post);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		given(postLikeRepository.findPostLikeByUserNicknameAndPostId(eq(nickname), eq(postId)))
			.willReturn(Optional.of(postLike));

		//when
		postLikeService.likeByPostId(postId, nickname);

		//then
		verify(postRepository).findPostAndUser(postId);
	}

	@DisplayName("게시물 좋아요 취소 테스트")
	@Test
	void cancelPostLikeByPostId() {
		//given
		Long postId = 1L;
		String nickname = "nickname";
		User user = createUser(nickname);
		Post post = createPost(user, Boundary.ALL);
		PostLike postLike = new PostLike(user, post);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(eq(post), eq(nickname));
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
		User user = createUser(nickname);
		Post post = createPost(user, Boundary.ALL);
		PostLike postLike = new PostLike(user, post);

		given(postRepository.findPostAndUser(anyLong())).willReturn(Optional.of(post));
		doNothing().when(postAccessValidator).isAvailablePost(eq(post), eq(nickname));
		given(postLikeRepository.findPostLikeByUserNicknameAndPostId(anyString(), anyLong()))
			.willThrow(new CustomException("해당 게시물은 존재하지 않습니다.", ErrorCode.NOT_FOUND));

		//then
		assertThatThrownBy(() -> postLikeService.unlikeByPostId(postId, nickname))
			.isInstanceOf(CustomException.class);
	}

	@DisplayName("게시물 작성자가 팔로우 공개 게시물 좋아요 숫자 조회 테스트")
	@Test
	void countPostLikeExceptionWithFollowOpenedPostAndWriter() {
		//given
		String postWriterNickname = "writer";
		User postWriter = createUser(postWriterNickname);
		Post followPost = createPost(postWriter, Boundary.FOLLOW);
		PostLike postLike = new PostLike(postWriter, followPost);
		followPost.addPostLike(postLike);

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(followPost));
		doNothing().when(postAccessValidator).isAvailablePost(eq(followPost), eq(postWriterNickname));

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
		Post publicPost = createPost(user, Boundary.ALL);
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
		Post postOpenedForFollower = createPost(user, Boundary.FOLLOW);
		long likeCnt = 30;
		for (long i = 0; i < likeCnt; i++) {
			PostLike postLike = new PostLike(user, postOpenedForFollower);
			postOpenedForFollower.addPostLike(postLike);
		}

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(postOpenedForFollower));
		doNothing().when(postAccessValidator).isAvailablePost(eq(postOpenedForFollower), eq(user.getNickname()));

		//when
		PostLikeCountResponse response = postLikeService.countPostLike(1L, user.getNickname());

		//then
		assertThat(response.getLikeCount()).isEqualTo(likeCnt);
	}

	private User createUser(String nickname) {
		return User.builder().nickname(nickname).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
	}

	private Post createPost(User user, Boundary boundary) {
		return Post.builder().user(user).postContent("content").tempSave(false).boundary(boundary).build();
	}
}