package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceUnitTest {

	@InjectMocks
	FollowService followService;

	@Mock
	FollowRepository followRepository;
	@Mock
	UserRepository userRepository;

	@DisplayName("사용자 자신을 팔로우하는 경우 예외 발생")
	@Test
	void selfFollowException() {
		//given
		String userNickname = "nickname";
		String followTargetNickname = "nickname";

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> followService.followUser(userNickname, followTargetNickname));

		//then
		assertEquals("팔로우는 다른 사용자만 가능합니다.", exception.getMessage());
		assertEquals(ErrorCode.USER_MISMATCH, exception.getErrorCode());
	}

	@DisplayName("이미 팔로우한 유저는 팔로우 엔티티를 저장하지 않는다.")
	@Test
	void followAlreadyFollowedUser() {
		//given
		String followerNickname = "follower";
		String followeeNickname = "following";
		User follower = createUserByNickname(followerNickname);
		User followee = createUserByNickname(followeeNickname);
		Follow follow = new Follow(follower, followee);

		when(followRepository.findFollowByFollowerNicknameAndFollowingNickname(eq(followerNickname),
			eq(followeeNickname))).thenReturn(Optional.of(follow));

		//when
		followService.followUser(followeeNickname, followerNickname);

		//then
		verify(followRepository).findFollowByFollowerNicknameAndFollowingNickname(followerNickname, followeeNickname);
		verify(userRepository, never()).findByNickname(anyString());
		verify(followRepository, never()).saveAndFlush(any());
	}

	@DisplayName("팔로우하지 않은 유저 팔로우 테스트")
	@Test
	void followTargetUser() {
		//given
		String userNickname = "nickname";
		String followTargetNickname = "followTarget";
		User follower = createUserByNickname(userNickname);
		User followee = createUserByNickname(followTargetNickname);
		Follow follow = new Follow(follower, followee);

		when(followRepository.findFollowByFollowerNicknameAndFollowingNickname(eq(userNickname),
			eq(followTargetNickname))).thenReturn(Optional.empty());
		when(userRepository.findByNickname(eq(userNickname))).thenReturn(Optional.of(follower));
		when(userRepository.findByNickname(eq(followTargetNickname))).thenReturn(Optional.of(followee));
		when(followRepository.saveAndFlush(any(Follow.class))).thenReturn(follow);

		//when
		followService.followUser(followTargetNickname, userNickname);

		//then
		verify(followRepository).findFollowByFollowerNicknameAndFollowingNickname(userNickname, followTargetNickname);
		verify(userRepository, times(2)).findByNickname(anyString());
		verify(followRepository).saveAndFlush(any(Follow.class));
	}

	private User createUserByNickname(String followerNickname) {
		return User.builder()
			.nickname(followerNickname)
			.phoneNumber("pn")
			.agreement(true)
			.role(UserRole.USER)
			.build();
	}
}