package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
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
import org.springframework.data.domain.Sort;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@Import(JpaAuditingConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

	@Autowired
	PostRepository postRepository;

	@Autowired
	EntityManager em;

	private User mj;
	private User jw;
	private User gw;
	private List<Post> posts = new ArrayList<>();
	private List<Photo> photos = new ArrayList<>();

	private static final int PAGE_SIZE = 20;
	private static final int TOTAL_POST_CNT = 30;

	@BeforeEach
	void setup() {
		mj = createTestMember("x@naver.com", "mj", "0101111", 1L);
		jw = createTestMember("y@naver.com", "jw", "0102222", 2L);
		gw = createTestMember("z@naver.com", "gw", "0103333", 3L);
		System.out.println("mj : " + mj.toString());

		createTestPostWithPhoto(TOTAL_POST_CNT, mj);
		createTestTempSavePostAndPhoto(mj);
		createTestTempSavePostAndPhoto(jw);
		createTestTempSavePostAndPhoto(gw);

		postRepository.saveAllAndFlush(posts);
		photos.forEach(photo -> em.persist(photo));
		em.flush();
		em.clear();
		log.info("========= 사전 데이터 끝 =========");
	}

	@DisplayName("커서 기반 페이징으로 Post 조회")
	@Test
	void cursorPagingPosts() {
		//given
		int size = 20;
		int secondSize = 35;
		List<Post> findPosts = em.createQuery(
				"select p from Post p where p.tempSave = false order by p.createdDate desc", Post.class)
			.getResultList();

		PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdDate"));
		PageRequest secondPageRequest = PageRequest.of(0, secondSize, Sort.by(Sort.Direction.DESC, "createdDate"));
		Long firstPostId = findPosts.get(0).getId();
		Long nextPostId = findPosts.get(19).getId();
		System.out.println("firstPostId = " + firstPostId);
		System.out.println("nextPostId = " + nextPostId);
		em.clear();

		//when
		Slice<Post> slicedPosts = postRepository.findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(
			firstPostId, Boundary.NONE, pageRequest);
		Slice<Post> nextSlicedPosts = postRepository.findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(
			nextPostId, Boundary.NONE, secondPageRequest);

		//then
		assertThat(slicedPosts).hasSize(size);
		assertThat(slicedPosts.hasNext()).isTrue();
		assertThat(slicedPosts.getNumber()).isEqualTo(0);
		assertThat(slicedPosts.getNumberOfElements()).isEqualTo(size);

		assertThat(nextSlicedPosts).hasSize(10);
		assertThat(nextSlicedPosts.hasNext()).isFalse();
		assertThat(nextSlicedPosts.getNumber()).isEqualTo(0);
		assertThat(nextSlicedPosts.getNumberOfElements()).isEqualTo(10);

	}

	@DisplayName("최신순으로 페이징한 게시물과 사진 목록 가져오기")
	@Test
	void getNewPostsWithPhoto() {
		// given
		int pageSize = PAGE_SIZE;

		// when
		System.out.println("==================== 1");
		PageRequest pageRequest1 = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
		Slice<Post> pagePosts1 = postRepository.findPostsAndPhotoByBoundaryAll(Boundary.ALL, pageRequest1);
		System.out.println("==================== 2");
		PageRequest pageRequest2 = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
		Slice<Post> pagePosts2 = postRepository.findPostsAndPhotoByBoundaryAll(Boundary.ALL,
			pageRequest2);

		// then
		pagePosts1.forEach(post -> assertThat(post.getUser().getNickname()).isEqualTo("mj"));
		pagePosts2.forEach(post -> assertThat(post.getUser().getNickname()).isEqualTo("mj"));

		assertThat(pagePosts1.getSize()).isEqualTo(pageSize);
		assertThat(pagePosts2.getSize()).isEqualTo(pageSize);
		// assertThat(pagePosts1.getTotalElements()).isEqualTo(TOTAL_POST_CNT);
		// assertThat(pagePosts1.getTotalPages()).isEqualTo(TOTAL_POST_CNT / PAGE_SIZE + 1);

		for (Post post : pagePosts1) {
			log.info("------------- post.Content = " + post.getPostContent() + " -------------");
			log.info("post.CreatedDate = " + post.getCreatedDate());
			System.out.println("======= 사진 가져오기 =======");
			List<Photo> photos = post.getPhotos();
			System.out.println("======= 사진 가져오기 완료 =======");
			log.info("photos.size = " + photos.size());
			for (Photo photo : photos) {
				log.info("photo.url = " + photo.getPhotoUrl());
				log.info("photo.name = " + photo.getName());
			}
		}
	}

	private void createTestTempSavePostAndPhoto(User user) {
		Post post = new Post(user, "임시저장 게시물 - " + user.getNickname(), 0L, Boundary.ALL, true);
		Photo photo1 = new Photo(post, "임시저장 url 1", "임시저장 photo 1-");
		Photo photo2 = new Photo(post, "임시저장 url 2", "임시저장 photo 2-");
		post.addPhoto(photo1);
		post.addPhoto(photo2);
		posts.add(post);
		photos.add(photo1);
		photos.add(photo2);
	}

	private void createTestPostWithPhoto(int postCnt, User user) {
		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(user, "" + i, 0L, Boundary.ALL, false);
			Photo photo1 = new Photo(post, "url 1-" + user.getNickname() + i, "photo 1-" + user.getNickname() + i);
			Photo photo2 = new Photo(post, "url 2-" + user.getNickname() + i, "photo 2-" + user.getNickname() + i);
			post.addPhoto(photo1);
			post.addPhoto(photo2);
			posts.add(post);
			photos.add(photo1);
			photos.add(photo2);
		}
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLoginId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		return user;
	}
}