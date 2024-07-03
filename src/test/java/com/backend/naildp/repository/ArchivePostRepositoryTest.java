package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArchivePostRepositoryTest {

	@Autowired
	ArchivePostRepository archivePostRepository;

	@Autowired
	EntityManager em;

	@BeforeEach
	void setup() {
		User user = createTestMember("mj@naver.com", "mj", "0101111", 1L);
		User writer = createTestMember("writer@naver.com", "writer", "0102222", 2L);

		// 게시물 생성
		List<Post> userPosts = createTestPostWithPhoto(5, user);
		List<Post> writerPosts = createTestPostWithPhoto(10, writer);

		// 아카이브 생성
		Archive archive = createTestArchive(user, "mj의 전체 공개 아카이브", Boundary.ALL);

		// 아카이브에 게시물 저장
		savePostInArchive(writerPosts, archive);

		em.clear();
	}

	@DisplayName("아카이브에 저장한 게시물 조회 테스트")
	@Test
	void findSavedPostInArchive() {
		//given
		String userNickname = "mj";
		String writerNickname = "writer";

		//when
		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(userNickname);
		List<Archive> archives = archivePosts.stream()
			.map(ArchivePost::getArchive)
			.distinct()
			.collect(Collectors.toList());
		List<User> writers = archivePosts.stream()
			.map(ArchivePost::getPost)
			.map(Post::getUser)
			.collect(Collectors.toList());

		//then
		archives.forEach(archive -> assertThat(archive.getName()).isEqualTo("mj의 전체 공개 아카이브"));
		archives.forEach(archive -> assertThat(archive.getBoundary()).isEqualTo(Boundary.ALL));
		archives.forEach(archive -> assertThat(archive.getUser().getNickname()).isEqualTo(userNickname));
		assertThat(archives.size()).isEqualTo(1);

		writers.forEach(user -> assertThat(user.getNickname()).isEqualTo(writerNickname));
		writers.forEach(user -> assertThat(user.getPhoneNumber()).isEqualTo("0102222"));
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLoginId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		return user;
	}

	private List<Post> createTestPostWithPhoto(int postCnt, User user) {
		List<Photo> photos = new ArrayList<>();
		List<Post> posts = new ArrayList<>();

		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(user, "" + i, 0L, Boundary.ALL, false);
			Photo photo1 = new Photo(post, "thumbnailUrl-" + user.getNickname() + i,
				"thumbnailName-" + user.getNickname() + i);
			Photo photo2 = new Photo(post, "subUrl-" + user.getNickname() + i, "subName" + user.getNickname() + i);
			post.addPhoto(photo1);
			post.addPhoto(photo2);
			posts.add(post);
			photos.add(photo1);
			photos.add(photo2);
		}

		for (Post post : posts) {
			em.persist(post);
		}
		for (Photo photo : photos) {
			em.persist(photo);
		}
		return posts;
	}

	private Archive createTestArchive(User user, String archiveName, Boundary boundary) {
		Archive archive = new Archive(user, archiveName, boundary);
		em.persist(archive);
		return archive;
	}

	private void savePostInArchive(List<Post> writerPosts, Archive archive) {
		List<ArchivePost> archivePosts = new ArrayList<>();
		for (Post post : writerPosts) {
			archivePosts.add(new ArchivePost(archive, post));
		}
		archivePostRepository.saveAllAndFlush(archivePosts);
	}
}