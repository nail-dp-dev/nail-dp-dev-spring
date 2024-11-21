package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

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
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
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

	private User testUser;

	private Archive archive1;
	private Archive archive2;
	private Archive archive3;

	private Post post;

	@BeforeEach
	public void setUp() {
		// given
		testUser = createUser("testUser");

		archive1 = createArchive(testUser, Boundary.ALL, 1);
		archive2 = createArchive(testUser, Boundary.FOLLOW, 2);
		archive3 = createArchive(testUser, Boundary.NONE, 3);

		post = createPostByUser(testUser, Boundary.ALL, 1);
		ArchivePost archivePost1 = createArchivePost(archive1, post);
		ArchivePost archivePost2 = createArchivePost(archive2, post);

	}

	@Test
	@DisplayName("사용자 닉네임으로 아카이브 리스트 조회 - 기본 조회")
	void findArchiveInfosByUserNickname1() {
		// when
		Slice<ArchiveMapping> archives = archiveRepository.findArchiveInfosByUserNickname("testUser",
			PageRequest.of(0, 10));

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(3);
		assertThat(archives.getContent().get(0).getName()).isEqualTo("archive3");
	}

	@Test
	@DisplayName("사용자 닉네임과 아카이브 ID로 아카이브 리스트 조회 - ID 기준 조회")
	void findArchiveInfosByIdAndUserNickname2() {
		// when
		Slice<ArchiveMapping> archives = archiveRepository.findArchiveInfosByIdAndUserNickname("testUser",
			archive2.getId(), PageRequest.of(0, 10));

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(1);
		assertThat(archives.getContent().get(0).getName()).isEqualTo("archive1");
	}

	@Test
	@DisplayName("(다른 유저 아카이브 조회)사용자 닉네임으로 NONE이 아닌 아카이브 리스트 조회")
	void findArchiveInfosWithoutNone1() {
		// when
		Slice<ArchiveMapping> archives = archiveRepository.findArchiveInfosWithoutNone("testUser",
			PageRequest.of(0, 10));

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(2);
		assertThat(archives.getContent().get(0).getName()).isEqualTo("archive2");
		assertThat(archives.getContent().get(1).getName()).isEqualTo("archive1");
	}

	@Test
	@DisplayName("(다른 유저 아카이브 조회)사용자 닉네임과 아카이브 ID로 NONE이 아닌 아카이브 정보 조회")
	void findArchiveInfosByIdWithoutNone2() {
		// when
		Slice<ArchiveMapping> archives = archiveRepository.findArchiveInfosByIdWithoutNone("testUser", archive2.getId(),
			PageRequest.of(0, 10));

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(1);
		assertThat(archives.getContent().get(0).getName()).isEqualTo("archive1");
	}

	@Test
	@DisplayName("저장된 게시물이 포함된 아카이브 조회")
	void findSavedArchiveByPage() {
		// when
		Slice<ArchiveMapping> archives = archiveRepository.findSavedArchiveByPage("testUser", post.getId(),
			PageRequest.of(0, 10));

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(2);
		assertThat(archives.getContent().get(0).getName()).isEqualTo("archive2");
		assertThat(archives.getContent().get(1).getName()).isEqualTo("archive1");

	}

	@Test
	@DisplayName("저장된 게시물이 포함된 아카이브 조회")
	void findSavedArchiveByIdAndPage() {
		// when
		Slice<ArchiveMapping> archives = archiveRepository.findSavedArchiveByIdAndPage("testUser", post.getId(),
			archive1.getId(),
			PageRequest.of(0, 10));

		// then
		assertThat(archives).isNotNull();
		assertThat(archives.getContent()).hasSize(0);

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
		Archive archive = new Archive(user, "archive" + i, boundary);
		archiveRepository.saveAndFlush(archive);
		return archive;
	}

	private ArchivePost createArchivePost(Archive archive, Post post) {
		ArchivePost archivePost = new ArchivePost(archive, post);
		archivePostRepository.saveAndFlush(archivePost);
		return archivePost;
	}

}
