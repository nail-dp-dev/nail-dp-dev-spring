package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

@SpringBootTest
@Transactional
class UserInfoServiceTest {

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ArchivePostRepository archivePostRepository;

	@Autowired
	private FollowRepository followRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private ArchiveRepository archiveRepository;

	private User myUser;
	private User otherUser;
	private Post post;
	private ArchivePost archivePost;

	@BeforeEach
	void setUp() {
		myUser = createUser("myUser");
		otherUser = createUser("otherUser");

		post = createPost(otherUser, Boundary.ALL);
		Post myPost = createPost(myUser, Boundary.NONE);

		Archive otherArchive = archiveRepository.save(new Archive(otherUser, "otherArchive", Boundary.ALL));

		archivePostRepository.save(new ArchivePost(otherArchive, post));
		archivePostRepository.save(new ArchivePost(otherArchive, myPost));

	}

	@Test
	@DisplayName("다른 사용자 정보 가져오기 - 팔로우한 경우")
	void getOtherUserInfo_Success() {
		//given
		followRepository.save(new Follow(myUser, otherUser));

		//when
		UserInfoResponseDto response = userInfoService.getOtherUserInfo(myUser.getNickname(), otherUser.getNickname());

		//then
		assertThat(response.getNickname()).isEqualTo(otherUser.getNickname());
		assertThat(response.getPoint()).isEqualTo(null);
		assertThat(response.getProfileUrl()).isEqualTo(otherUser.getThumbnailUrl());
		assertThat(response.getPostsCount()).isEqualTo(1);
		assertThat(response.getSaveCount()).isEqualTo(1);
		assertThat(response.getFollowerCount()).isEqualTo(1);
		assertThat(response.getFollowingCount()).isEqualTo(0);
		assertThat(response.getFollowingStatus()).isTrue();
	}

	@Test
	@DisplayName("존재하지 않는 사용자 정보 조회")
	void getOtherUserInfo_UserNotFound() {
		//given
		String notFoundUser = "notFoundUser";

		//then
		CustomException exception = assertThrows(CustomException.class,
			() -> userInfoService.getOtherUserInfo(myUser.getNickname(), notFoundUser));

		assertThat(exception.getMessage()).isEqualTo("nickname 으로 회원을 찾을 수 없습니다.");
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("다른 사용자 정보 가져오기 - 팔로우 아닌 경우")
	void getOtherUserInfo_NoFollowers() {
		followRepository.deleteAll();
		// when
		UserInfoResponseDto response = userInfoService.getOtherUserInfo(myUser.getNickname(), otherUser.getNickname());

		// then
		assertThat(response.getNickname()).isEqualTo(otherUser.getNickname());
		assertThat(response.getPoint()).isEqualTo(null);
		assertThat(response.getProfileUrl()).isEqualTo(otherUser.getThumbnailUrl());
		assertThat(response.getPostsCount()).isEqualTo(1);
		assertThat(response.getSaveCount()).isEqualTo(1);
		assertThat(response.getFollowerCount()).isEqualTo(0);
		assertThat(response.getFollowingCount()).isEqualTo(0);
		assertThat(response.getFollowingStatus()).isFalse();
	}

	private User createUser(String postWriter) {
		User user = User.builder()
			.nickname(postWriter)
			.phoneNumber("pn")
			.agreement(true)
			.thumbnailUrl("")
			.role(UserRole.USER)
			.build();
		userRepository.save(user);
		return user;
	}

	private Post createPost(User postWriter, Boundary boundary) {
		Post post = Post.builder()
			.user(postWriter)
			.postContent(postWriter.getNickname() + "content")
			.tempSave(false)
			.boundary(boundary)
			.build();
		postRepository.save(post);
		return post;
	}

}