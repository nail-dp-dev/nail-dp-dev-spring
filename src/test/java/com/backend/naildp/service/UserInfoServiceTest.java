package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.ProfileRepository;
import com.backend.naildp.repository.UserRepository;

class UserInfoServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private ArchivePostRepository archivePostRepository;
	@Mock
	private FollowRepository followRepository;
	@InjectMocks
	private UserInfoService userInfoService;

	private User user1;
	private User user2;
	private Profile profile1;
	private Post post1;
	private Post post2;
	private Post post3;
	private Archive archive1;
	private ArchivePost archivePost1;
	private ArchivePost archivePost2;
	private Follow follow1;

	private int followerCount;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user1 = new User("alswl", "010-1234-5678", 1000L, UserRole.USER);
		profile1 = new Profile(user1, "alswl.profileUrl.jpg", "name", true);
		post1 = new Post(user1, "alswl postContent", 0L, Boundary.ALL, false);

		user2 = new User("jw", "010-9876-5432", 0L, UserRole.USER);
		post2 = new Post(user2, "jw postContent", 0L, Boundary.ALL, false);
		post3 = new Post(user2, "jw postContent", 0L, Boundary.ALL, true);

		archive1 = new Archive(user1, "alswl archive", Boundary.ALL);
	}

	@DisplayName("저장한 포스트의 공개범위가 전체일 떄")
	@Test
	void testGetUserInfo_PostBoundaryAll() {
		// Given
		archivePost1 = new ArchivePost(archive1, post2);
		followerCount = 0;

		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(Collections.emptyList());
		given(profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user1)).willReturn(profile1);
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getNickname()).isEqualTo("alswl");
		assertThat(userInfoResponseDto.getPoint()).isEqualTo(1000L);
		assertThat(userInfoResponseDto.getProfileUrl()).isEqualTo("alswl.profileUrl.jpg");
		assertThat(userInfoResponseDto.getPostsCount()).isEqualTo(1);
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(1);
		assertThat(userInfoResponseDto.getFollowerCount()).isEqualTo(followerCount);
	}

	@DisplayName("저장한 포스트의 공개범위가 팔로워일 때_notFollowing")
	@Test
	void testGetUserInfo_PostBoundaryFollow1() {
		// Given
		post2 = new Post(user2, "jw postContent", 0L, Boundary.FOLLOW, false);
		archivePost1 = new ArchivePost(archive1, post2);

		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(Collections.emptyList());
		given(profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user1)).willReturn(profile1);
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(0);
	}

	@DisplayName("저장한 포스트의 공개범위가 팔로워일 때_Following")
	@Test
	void testGetUserInfo_PostBoundaryFollow2() {
		// Given
		post2 = new Post(user2, "jw postContent", 0L, Boundary.FOLLOW, false);
		Post post4 = new Post(user2, "jw postContent", 0L, Boundary.FOLLOW, false);
		archivePost1 = new ArchivePost(archive1, post2);
		archivePost2 = new ArchivePost(archive1, post4);

		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1, archivePost2));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(
			List.of(user2.getNickname(), "user3"));
		given(profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user1)).willReturn(profile1);
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(2);
	}

	@DisplayName("저장한 포스트의 공개범위가 비공개일 때")
	@Test
	void testGetUserInfo_PostBoundaryNone() {
		// Given
		post2 = new Post(user2, "jw postContent", 0L, Boundary.NONE, false);
		archivePost1 = new ArchivePost(archive1, post2);
		followerCount = 1;
		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(List.of(user2.getNickname()));
		given(profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user1)).willReturn(profile1);
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(0);
	}

}