package com.backend.naildp.service;

import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.LoginRequestDto;
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
		Assertions.assertThatThrownBy(() -> postLikeService.unlikeByPostId(postId, nickname))
			.isInstanceOf(CustomException.class);
	}
}