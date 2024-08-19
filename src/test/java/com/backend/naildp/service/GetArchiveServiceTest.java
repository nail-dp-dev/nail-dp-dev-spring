package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.archive.FollowArchiveResponseDto;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

@Disabled
@SpringBootTest
@Transactional
public class GetArchiveServiceTest {

	@Autowired
	private ArchiveService archiveService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ArchiveRepository archiveRepository;
	@Autowired
	private FollowRepository followRepository;
	@Autowired
	private PostRepository postRepository;
	private User user1;
	private User user2;
	private User user3;

	private PostLike postLike;

	private Post post;

	@BeforeEach
	void setUp() {

		user1 = createUser("mj");
		user2 = createUser("jw");
		user3 = createUser("jw2");

		for (int i = 1; i <= 10; i++) {
			createArchive(user2, Boundary.ALL, i);  // user1이 user2 팔로잉
		}
		createArchive(user1, Boundary.ALL, 1);

		createFollow(user1, user2);
		createArchive(user3, Boundary.ALL, 1);  // user1이 user2 팔로잉

	}

	@Test
	@DisplayName("팔로잉 없을때 -none")
	void testGetFollowingArchives1() {

		PostSummaryResponse response = archiveService.getFollowingArchives("jw", 5, -1L);

		assertThat(response).isNotNull();
		assertThat(response.getPostSummaryList()).hasSize(0);
		assertThat(response.getCursorId()).isEqualTo(-1L);

	}

	@Test
	@DisplayName("팔로잉 게시물 조회 - 1명")
	void testGetFollowingArchives2() {

		PostSummaryResponse response = archiveService.getFollowingArchives("mj", 5, -1L);
		Slice<FollowArchiveResponseDto> contents = (Slice<FollowArchiveResponseDto>)response.getPostSummaryList();

		assertThat(response).isNotNull();
		assertThat(contents.getSize()).isEqualTo(5);
		assertThat(contents.getContent().get(0).getArchiveId()).isEqualTo(10L);
		assertThat(contents.getContent().get(0).getArchiveCount()).isEqualTo(10L);

	}

	@Test
	@DisplayName("팔로잉 게시물 조회 - 2명")
	void testGetFollowingArchives3() {
		createFollow(user1, user3);

		PostSummaryResponse response = archiveService.getFollowingArchives("mj", 5, -1L);
		Slice<FollowArchiveResponseDto> contents = (Slice<FollowArchiveResponseDto>)response.getPostSummaryList();

		assertThat(response).isNotNull();
		assertThat(contents.getSize()).isEqualTo(5);
		assertThat(contents.getContent().get(0).getArchiveId()).isEqualTo(12L);
		assertThat(contents.getContent().get(0).getArchiveCount()).isEqualTo(1);

	}

	private User createUser(String postWriter) {
		User user = User.builder().nickname(postWriter).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		userRepository.saveAndFlush(user);
		return user;
	}

	private void createPost(User postWriter, String content, int i) {
		Post post = Post.builder()
			.user(postWriter)
			.postContent(content + i)
			.tempSave(false)
			.boundary(Boundary.ALL)
			.build();
		postRepository.save(post);
		Photo photo = new Photo(post, new FileRequestDto("file" + i + ".jpg", 12345L, "fileUrl" + i + ".jpg"));
		post.addPhoto(photo);
	}

	private void createArchive(User user, Boundary boundary, int i) {
		Archive archive = new Archive(user, user.getNickname() + "archive" + i, boundary);
		archiveRepository.save(archive);
	}

	// private void addArchivePost(Archive archive, Post post) {
	// 	ArchivePost archivePost = new ArchivePost(archive, post);
	// 	em.persist(archivePost);
	// }

	private void createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		followRepository.save(follow);
	}

}
