package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
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

	private static final int PAGE_SIZE = 30;
	private static final int TOTAL_POST_CNT = 30;

	@BeforeEach
	void setup() {
		mj = createTestMember("x@naver.com", "mj", "0101111", 1L);
		jw = createTestMember("y@naver.com", "jw", "0102222", 2L);
		gw = createTestMember("z@naver.com", "gw", "0103333", 3L);

		createFollow(jw, mj);

		createTestPostWithPhoto(TOTAL_POST_CNT, mj, Boundary.ALL);
		createTestPostWithPhoto(TOTAL_POST_CNT, gw, Boundary.ALL);
		createTestPostWithPhoto(TOTAL_POST_CNT, mj, Boundary.FOLLOW);
		createTestPostWithPhoto(TOTAL_POST_CNT, gw, Boundary.FOLLOW);
		createTestPostWithPhoto(TOTAL_POST_CNT, mj, Boundary.NONE);
		createTestPostWithPhoto(TOTAL_POST_CNT, gw, Boundary.NONE);

		createTestTempSavePostAndPhoto(mj);
		createTestTempSavePostAndPhoto(jw);
		createTestTempSavePostAndPhoto(gw);

		postRepository.saveAllAndFlush(posts);
		photos.forEach(photo -> em.persist(photo));
		em.flush();
		em.clear();
		log.info("========= 사전 데이터 끝 =========");
	}

	@DisplayName("팔로워 사용자는 전체공개이거나 팔로워 공개 게시물을 조회할 수 있다.")
	@Test
	void readablePostByFollowerIsFollowAndAll() {
		//given
		String followerUserNickname = "jw";
		int readablePostCount = TOTAL_POST_CNT * 3;
		PageRequest pageRequest = PageRequest.of(0, readablePostCount, Sort.by(Sort.Direction.DESC, "id"));
		List<User> followings = findFollowByFollowerNickname(followerUserNickname).stream()
			.map(Follow::getFollowing).toList();

		//when
		Slice<Post> recentPosts = postRepository.findRecentPostsByFollowing(followings, followerUserNickname, pageRequest);

		//then
		assertThat(recentPosts.getSize()).isEqualTo(readablePostCount);
		assertThat(recentPosts.getNumberOfElements()).isEqualTo(readablePostCount);
		assertThat(recentPosts).extracting("boundary").containsOnly(Boundary.FOLLOW, Boundary.ALL);
		assertThat(recentPosts).extracting("tempSave").containsOnly(false);
	}

	@DisplayName("일반 사용자는 전체공개 게시물만 조회 가능")
	@Test
	void readablePostByNormalUserIsAll() {
		//given
		String normalUserNickname = "normal";
		int readablePostCount = TOTAL_POST_CNT * 2;
		PageRequest pageRequest = PageRequest.of(0, readablePostCount, Sort.by(Sort.Direction.DESC, "id"));
		List<User> followings = findFollowByFollowerNickname(normalUserNickname).stream()
			.map(Follow::getFollowing)
			.toList();

		//when
		Slice<Post> recentPosts = postRepository.findRecentPostsByFollowing(followings, normalUserNickname, pageRequest);

		//then
		assertThat(recentPosts.getSize()).isEqualTo(readablePostCount);
		assertThat(recentPosts.getNumberOfElements()).isEqualTo(readablePostCount);
		assertThat(recentPosts).extracting("boundary").containsOnly(Boundary.ALL);
		assertThat(recentPosts).extracting("tempSave").containsOnly(false);
	}

	@DisplayName("게시물 작성자는 전체공개 게시물과 팔로잉하는 사용자의 팔로우 공개 게시물을 조회 가능")
	@Test
	void readablePostByWriterIsFollowAndAll() {
		//given
		String writerNickname = "mj";
		int readablePostCnt = TOTAL_POST_CNT * 3;
		PageRequest pageRequest = PageRequest.of(0, readablePostCnt, Sort.by(Sort.Direction.DESC, "id"));
		List<User> followings = findFollowByFollowerNickname(writerNickname).stream()
			.map(Follow::getFollowing).toList();

		//when
		Slice<Post> recentPosts = postRepository.findRecentPostsByFollowing(followings, writerNickname, pageRequest);

		//then
		assertThat(recentPosts.getSize()).isEqualTo(readablePostCnt);
		assertThat(recentPosts.getNumberOfElements()).isEqualTo(readablePostCnt);
		assertThat(recentPosts).extracting("boundary").containsOnly(Boundary.FOLLOW, Boundary.ALL);
		assertThat(recentPosts).extracting("tempSave").containsOnly(false);
	}

	@DisplayName("팔로워 사용자는 전체공개이거나 팔로잉 사용자의 팔로워 공개 게시물을 조회할 수 있다. - 커서 페이징")
	@Test
	void readablePostByFollowerIsFollowAndAllWithUsingCursor() {
		//given
		String followerUserNickname = "jw";
		int firstPageElementCount = 1;
		int readablePostCount = TOTAL_POST_CNT * 3 - firstPageElementCount;
		PageRequest firstPageRequest = PageRequest.of(0, firstPageElementCount, Sort.by(Sort.Direction.DESC, "id"));
		PageRequest pageRequest = PageRequest.of(0, readablePostCount, Sort.by(Sort.Direction.DESC, "id"));
		List<User> followings = findFollowByFollowerNickname(followerUserNickname).stream()
			.map(Follow::getFollowing).toList();

		//when
		Long cursorPostId = postRepository.findRecentPostsByFollowing(followings, followerUserNickname, firstPageRequest)
			.getContent().get(firstPageElementCount - 1).getId();
		Slice<Post> secondRecentPosts = postRepository.findRecentPostsByIdAndFollowing(cursorPostId, followings,
			followerUserNickname, pageRequest);

		//then
		assertThat(secondRecentPosts.getSize()).isEqualTo(readablePostCount);
		assertThat(secondRecentPosts.getNumberOfElements()).isEqualTo(readablePostCount);
		assertThat(secondRecentPosts).extracting("boundary").containsOnly(Boundary.FOLLOW, Boundary.ALL);
		assertThat(secondRecentPosts).extracting("tempSave").containsOnly(false);
	}

	@DisplayName("일반 사용자는 전체공개 게시물을 조회할 수 있다. - 커서 페이징")
	@Test
	void readablePostByNormalUserIsAllWithUsingCursor() {
		//given
		String normalUserNickname = "normalUserNickname";
		int firstPageElementCount = 1;
		int readablePostCount = TOTAL_POST_CNT * 2 - firstPageElementCount;
		PageRequest firstPageRequest = PageRequest.of(0, firstPageElementCount, Sort.by(Sort.Direction.DESC, "id"));
		PageRequest pageRequest = PageRequest.of(0, readablePostCount, Sort.by(Sort.Direction.DESC, "id"));
		List<User> followings = findFollowByFollowerNickname(normalUserNickname).stream()
			.map(Follow::getFollowing).toList();

		//when
		Long cursorPostId = postRepository.findRecentPostsByFollowing(followings, normalUserNickname, firstPageRequest)
			.getContent().get(firstPageElementCount - 1).getId();
		Slice<Post> secondRecentPosts = postRepository.findRecentPostsByIdAndFollowing(cursorPostId, followings,
			normalUserNickname, pageRequest);

		//then
		assertThat(secondRecentPosts.getSize()).isEqualTo(readablePostCount);
		assertThat(secondRecentPosts.getNumberOfElements()).isEqualTo(readablePostCount);
		assertThat(secondRecentPosts).extracting("boundary").containsOnly(Boundary.ALL);
		assertThat(secondRecentPosts).extracting("tempSave").containsOnly(false);
	}

	@DisplayName("게시물 작성자는 전체공개 게시물과 팔로잉하는 사용자의 팔로우 공개 게시물을 조회할 수 있다. - 커서 페이징")
	@Test
	void readablePostByWriterIsFollowAndAllWithUsingCursor() {
		//given
		String writerNickname = "mj";
		int firstPageElementCount = 1;
		int readablePostCount = TOTAL_POST_CNT * 2 - firstPageElementCount;
		PageRequest firstPageRequest = PageRequest.of(0, firstPageElementCount, Sort.by(Sort.Direction.DESC, "id"));
		PageRequest pageRequest = PageRequest.of(0, readablePostCount, Sort.by(Sort.Direction.DESC, "id"));
		List<User> followings = findFollowByFollowerNickname(writerNickname).stream()
			.map(Follow::getFollowing).toList();

		//when
		Long cursorPostId = postRepository.findRecentPostsByFollowing(followings, writerNickname, firstPageRequest)
			.getContent().get(firstPageElementCount - 1).getId();
		Slice<Post> secondRecentPosts = postRepository.findRecentPostsByIdAndFollowing(cursorPostId, followings,
			writerNickname, pageRequest);

		//then
		assertThat(secondRecentPosts.getSize()).isEqualTo(readablePostCount);
		assertThat(secondRecentPosts.getNumberOfElements()).isEqualTo(readablePostCount);
		assertThat(secondRecentPosts).extracting("boundary").containsOnly(Boundary.FOLLOW, Boundary.ALL);
		assertThat(secondRecentPosts).extracting("tempSave").containsOnly(false);
	}

	private void createTestTempSavePostAndPhoto(User user) {
		Post post = new Post(user, "임시저장 게시물 - " + user.getNickname(), 0L, Boundary.ALL, true);
		posts.add(post);
	}

	private void createTestPostWithPhoto(int postCnt, User user, Boundary boundary) {
		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(user, "" + i, 0L, boundary, false);
			posts.add(post);
		}
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		em.persist(user);
		return user;
	}

	private void createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}

	private List<Follow> findFollowByFollowerNickname(String nickname) {
		return em.createQuery("select f from Follow f where f.follower.nickname = :follower",
				Follow.class)
			.setParameter("follower", nickname)
			.getResultList();
	}

	private Post findOldestPost() {
		return em.createQuery(
				"select p from Post p where p.tempSave = false and p.boundary <> 'NONE' order by p.id desc",
				Post.class)
			.setMaxResults(1)
			.getSingleResult();
	}
}