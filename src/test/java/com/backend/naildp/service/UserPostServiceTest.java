package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.userInfo.TempSaveResponseDto;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PhotoRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Disabled
@Slf4j
@SpringBootTest
@Transactional
@Import(JpaAuditingConfiguration.class)가
class UserPostServiceTest {

	@Autowired
	private UserPostService userPostService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private ArchivePostRepository archivePostRepository;

	@Autowired
	private FollowRepository followRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	private User user1;

	private PostLike postLike;

	private Post post;

	@BeforeEach
	void setUp() {
		// MySQL에서 AUTO_INCREMENT 리셋
		jdbcTemplate.execute("ALTER TABLE post AUTO_INCREMENT = 1");
		jdbcTemplate.execute("ALTER TABLE photo AUTO_INCREMENT = 1");

		user1 = new User("nickname1", "010-1234-5678", 0L, UserRole.USER);
		User user2 = new User("nickname2", "010-1234-5678", 0L, UserRole.USER);

		userRepository.save(user1);
		userRepository.save(user2);

		for (int i = 1; i <= 10; i++) {
			post = Post.builder()
				.user(user1)
				.photos(new ArrayList<>())
				.postLikes(new ArrayList<>())
				.postContent("content" + i)
				.boundary(Boundary.ALL)
				.tempSave(false)
				.tagPosts(new ArrayList<>())
				.build();

			postRepository.save(post);
			Photo photo = new Photo(post, new FileRequestDto("file1" + i + ".jpg", 12345L, "fileUrl1" + i + ".jpg"));

			post.addPhoto(photo);

			photoRepository.save(photo);

			postLike = new PostLike(user1, post);

			post.addPostLike(postLike);
			postLikeRepository.save(postLike);

		}
		long postCount = postRepository.count();
		assertThat(postCount).isEqualTo(10);

		long postLikeCount = postLikeRepository.count();
		assertThat(postLikeCount).isEqualTo(10);

	}

	@Test
	@DisplayName("유저 게시물 전체 조회 - 첫 페이지")
	void testGetUserPosts() {
		PostSummaryResponse response = userPostService.getUserPosts("nickname1", 5, -1L);

		assertThat(response).isNotNull();
		assertThat(response.getPostSummaryList()).hasSize(5);
		assertThat(response.getPostSummaryList().getContent().get(0).getPostId()).isEqualTo(10L);
	}

	@Test
	@DisplayName("유저 게시물 전체 조회 - 다음 페이지")
	void testGetUserPosts_UserNotFound() {
		PostSummaryResponse response = userPostService.getUserPosts("nickname1", 5, 6L);

		assertThat(response).isNotNull();
		assertThat(response.getPostSummaryList()).hasSize(5);
		assertThat(response.getPostSummaryList().getContent().get(0).getPostId()).isEqualTo(5L);
	}

	@Test
	@DisplayName("유저 게시물 좋아요 조회 - 없을 때")
	void testGetLikedUserPosts() {
		postLikeRepository.deleteAll();
		PostSummaryResponse response = userPostService.getLikedUserPosts("nickname1", 5, -1L);

		assertThat(response).isNotNull();
		assertThat(response.getPostSummaryList()).hasSize(0);

	}

	@Test
	@DisplayName("유저 게시물 좋아요 조회 - 첫 페이지")
	void testGetLikedUserPosts_None() {
		PostSummaryResponse response = userPostService.getLikedUserPosts("nickname1", 5, -1L);

		assertThat(response).isNotNull();
		assertThat(response.getPostSummaryList()).hasSize(5);
		assertThat(response.getPostSummaryList().getContent().get(0).getPostId()).isEqualTo(10L);

	}

	@Test
	@DisplayName("임시저장 조회 - 없을 때")
	void testGetTempPost_None() {
		TempSaveResponseDto response = userPostService.getTempPost("nickname1");

		assertThat(response).isNull(); // 현재 임시 저장된 포스트가 없으므로 null이어야 함
	}

	@Test
	@DisplayName("임시저장 조회 - 있을 때")
	void testGetTempPost() {
		Post tempPost = Post.builder()
			.user(user1)
			.photos(new ArrayList<>())
			.postContent("content")
			.boundary(Boundary.ALL)
			.tempSave(true)
			.tagPosts(new ArrayList<>())
			.build();
		postRepository.save(tempPost);

		TempSaveResponseDto response = userPostService.getTempPost("nickname1");

		assertThat(response).isNotNull();
		assertThat(response.getPostId()).isEqualTo(tempPost.getId());
	}

}