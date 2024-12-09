package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.dto.archive.FollowArchiveResponseDto;
import com.backend.naildp.dto.archive.UserArchiveResponseDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

@DataJpaTest
@Transactional
@Import({JpaAuditingConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ArchiveRepositoryTest {

	@Autowired
	private ArchiveRepository archiveRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private ArchivePostRepository archivePostRepository;

	@Autowired
	private FollowRepository followRepository;

	private User testUser;
	private User testUser2;

	private Archive archive1;
	private Archive archive2;
	private Archive archive3;

	private Post post;

	@BeforeEach
	public void setUp() {
		// given
		testUser = createUser("testUser");
		testUser2 = createUser("testUser2");

		createFollow(testUser2, testUser);
		Archive archive4 = createArchive(testUser2, Boundary.ALL, 1);

		archive1 = createArchive(testUser, Boundary.ALL, 1);
		archive2 = createArchive(testUser, Boundary.FOLLOW, 2);
		archive3 = createArchive(testUser, Boundary.NONE, 3);

		post = createPostByUser(testUser, Boundary.ALL, 1);
		ArchivePost archivePost1 = createArchivePost(archive1, post);
		ArchivePost archivePost2 = createArchivePost(archive2, post);
		Archive archivePost3 = createArchive(testUser2, Boundary.ALL, 1);

	}

	@Test
	@DisplayName("사용자 닉네임으로 아카이브 리스트 조회")
	void findUserArchives() {
		// when
		Slice<UserArchiveResponseDto> archives = archiveRepository.findUserArchives("testUser", -1L, 10);

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(3);
		assertThat(archives.getContent().get(0).getArchiveName()).isEqualTo("archive3");
	}

	@Test
	@DisplayName("다른 사용자 아카이브 조회")
	void findOtherArchives() {
		// when
		Slice<UserArchiveResponseDto> archives = archiveRepository.findOtherUserArchives("testUser", -1L, 10);

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(2); // NONE 제외
		assertThat(archives.getContent().get(0).getArchiveName()).isEqualTo("archive2");
	}

	@Test
	@DisplayName("팔로잉한 사용자 아카이브 조회")
	void findFollowingArchives() {
		// given
		List<String> followingNicknames = List.of("testUser2");

		// when
		Slice<FollowArchiveResponseDto> archives = archiveRepository.findFollowingArchives(followingNicknames, -1L, 10);

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(1);
		assertThat(archives.getContent().get(0).getNickname()).isEqualTo("testUser2");
	}

	@Test
	@DisplayName("저장된 게시물이 포함된 아카이브 조회")
	void findSavedArchiveByPage() {
		// when
		Slice<UserArchiveResponseDto> archives = archiveRepository.findSavedArchives("testUser", post.getId(), -1L, 10);
		PageRequest.of(0, 10);

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(2);
		assertThat(archives.getContent().get(0).getArchiveName()).isEqualTo("archive2");
		assertThat(archives.getContent().get(1).getArchiveName()).isEqualTo("archive1");

	}

	private User createUser(String postWriter) {
		User user = User.builder()
			.nickname(postWriter)
			.phoneNumber("pn")
			.agreement(true)
			.thumbnailUrl("")
			.role(UserRole.USER)
			.build();
		userRepository.saveAndFlush(user);
		return user;
	}

	private Post createPostByUser(User user, Boundary boundary, int i) {
		Post post = Post.builder()
			.user(user)
			.postContent("content" + i)
			.tempSave(false)
			.boundary(boundary)
			.build();

		Photo photo = new Photo(post, new FileRequestDto(".jpg", 1L, ".jpg"));
		post.addPhoto(photo);

		postRepository.save(post);
		photoRepository.save(photo);

		return post;
	}

	private Archive createArchive(User user, Boundary boundary, int i) {
		Archive archive = new Archive(user, "archive" + i, boundary, "archive" + i + "jpg");
		archiveRepository.saveAndFlush(archive);
		return archive;
	}

	private ArchivePost createArchivePost(Archive archive, Post post) {
		ArchivePost archivePost = new ArchivePost(archive, post);
		archivePostRepository.saveAndFlush(archivePost);
		return archivePost;
	}

	private Follow createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		followRepository.saveAndFlush(follow);

		return follow;
	}
}
