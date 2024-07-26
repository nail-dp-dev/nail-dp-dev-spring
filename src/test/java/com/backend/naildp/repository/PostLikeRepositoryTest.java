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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostLikeRepositoryTest {

	@Autowired
	PostLikeRepository postLikeRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	EntityManager em;

	@BeforeEach
	void setup() {
		User user1 = createTestMember("user1@naver.com", "user1", "0100000", 1L);
		User user2 = createTestMember("user2@naver.com", "user2", "0109999", 2L);
		User writer1 = createTestMember("writer1@naver.com", "writer1", "0101111", 3L);
		User writer2 = createTestMember("writer2@naver.com", "writer2", "0102222", 4L);
		User writer3 = createTestMember("writer3@naver.com", "writer3", "0103333", 5L);

		//팔로우 생성
		createFollow(user1, writer1);

		// 게시물 생성
		List<Post> publicPostsByWriter1 = createTestPostWithPhoto(writer1, 5, 5, Boundary.ALL);
		List<Post> followPostsByWriter1 = createTestPostWithPhoto(writer1, 5, 5, Boundary.FOLLOW);
		List<Post> privatePostsByWriter1 = createTestPostWithPhoto(writer1, 5, 5, Boundary.NONE);
		List<Post> publicPostsByWriter2 = createTestPostWithPhoto(writer2, 6, 6, Boundary.ALL);
		List<Post> publicPostsByWriter3 = createTestPostWithPhoto(writer3, 7, 7, Boundary.ALL);

		postLikeRepository.saveAllAndFlush(createPostLikes(publicPostsByWriter1, user1));
		postLikeRepository.saveAllAndFlush(createPostLikes(followPostsByWriter1, user1));
		postLikeRepository.saveAllAndFlush(createPostLikes(privatePostsByWriter1, user1));
		postLikeRepository.saveAllAndFlush(createPostLikes(publicPostsByWriter2, user2));
		postLikeRepository.saveAllAndFlush(createPostLikes(publicPostsByWriter3, user2));

		em.clear();
	}

	@DisplayName("한명의 게시물을 좋아요한 유저 검증 테스트")
	@Test
	void validatePostLikeInfo() {
		//given
		String nickname = "user1";
		String writerNickname = "writer1";
		User user = findUserByNickname(nickname);
		User writer = findUserByNickname(writerNickname);

		//when
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);

		//then
		assertThat(postLikes).extracting("user").containsOnly(user);
		assertThat(postLikes).extracting("post").extracting("user").containsOnly(writer);
	}

	@DisplayName("두명의 게시물을 좋아요한 유저 검증 테스트")
	@Test
	void validatePostLikeByUser2() {
		//given
		String nickname = "user2";
		User findUser = findUserByNickname(nickname);
		User writer2 = findUserByNickname("writer2");
		User writer3 = findUserByNickname("writer3");

		//when
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);

		//then
		assertThat(postLikes).extracting("user").containsOnly(findUser);
		assertThat(postLikes).extracting("post").extracting("user").containsOnly(writer2, writer3);
	}

	@DisplayName("좋아요한 모든 게시물 좋아요 취소 테스트")
	@Test
	void deletePostLike() {
		//given
		String nickname = "user1";
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);

		//when
		postLikes.forEach(postLike -> postLikeRepository.deletePostLikeById(postLike.getId()));

		//then
		List<PostLike> deletedPostLikes = postLikeRepository.findAllByUserNickname(nickname);
		assertThat(deletedPostLikes.size()).isEqualTo(0);
	}

	@DisplayName("공개 범위별 좋아요게시물 페이징 조회 테스트")
	@Test
	void findPagedPostLikes() {
		//given
		int pageNumber = 0;
		int pageSize = 20;
		String nickname = "user1";
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize);

		//when
		Page<PostLike> pagedPostLikesByBoundaryNotNone = postLikeRepository.findPagedPostLikesByBoundaryOpened(
			pageRequest, nickname, Boundary.NONE);
		Page<PostLike> pagedPostLikesByBoundaryNotFollowed = postLikeRepository.findPagedPostLikesByBoundaryOpened(
			pageRequest, nickname, Boundary.FOLLOW);
		Page<PostLike> pagedPostLikesByBoundaryNotAll = postLikeRepository.findPagedPostLikesByBoundaryOpened(
			pageRequest, nickname, Boundary.ALL);

		//then
		assertThat(pagedPostLikesByBoundaryNotNone).hasSize(10);
		assertThat(pagedPostLikesByBoundaryNotFollowed).hasSize(10);
		assertThat(pagedPostLikesByBoundaryNotAll).hasSize(10);

		assertThat(pagedPostLikesByBoundaryNotNone.getNumber()).isEqualTo(0);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getNumber()).isEqualTo(0);
		assertThat(pagedPostLikesByBoundaryNotAll.getNumber()).isEqualTo(0);

		assertThat(pagedPostLikesByBoundaryNotNone.getSize()).isEqualTo(20);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getSize()).isEqualTo(20);
		assertThat(pagedPostLikesByBoundaryNotAll.getSize()).isEqualTo(20);

		assertThat(pagedPostLikesByBoundaryNotNone.getTotalElements()).isEqualTo(10);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getTotalElements()).isEqualTo(10);
		assertThat(pagedPostLikesByBoundaryNotAll.getTotalElements()).isEqualTo(10);

		assertThat(pagedPostLikesByBoundaryNotNone.getTotalPages()).isEqualTo(1);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getTotalPages()).isEqualTo(1);
		assertThat(pagedPostLikesByBoundaryNotAll.getTotalPages()).isEqualTo(1);
	}

	@DisplayName("전체공개이거나 팔로우 공개이고 임시저장이 아닌 PostLike - 오프셋 페이지네이션 테스트")
	@Test
	void offsetPaginationPostLikes() {
		//given
		String nickname = "user1";
		PageRequest pageRequest = createPageRequest(0, 50);
		List<User> followingUsers = findFollowingUsersByNickname(nickname);

		//when
		Slice<PostLike> postLikesByFollowing = postLikeRepository.findPostLikesByFollowing(nickname, followingUsers,
			pageRequest);
		List<PostLike> postLikes = postLikesByFollowing.getContent();

		//then
		assertThat(postLikesByFollowing).extracting("post")
			.extracting("boundary")
			.containsOnly(Boundary.ALL, Boundary.FOLLOW);
		assertThat(postLikesByFollowing).extracting("post").extracting("tempSave").containsOnly(false);
	}

	@DisplayName("전체공개이거나 팔로우 공개이고 임시저장이 아닌 PostLike - 커서 페이지네이션 테스트")
	@Test
	void cursorPaginationPostLikes() {
		//given
		String nickname = "user1";
		PageRequest pageRequest = createPageRequest(0, 20);
		List<User> followingUsers = findFollowingUsersByNickname(nickname);
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		int size = postLikes.size();
		PostLike oldestPostLike = postLikes.get(size - 1);

		//when
		Slice<PostLike> postLikesByIdAndFollowing = postLikeRepository.findPostLikesByIdAndFollowing(nickname,
			oldestPostLike.getId(), followingUsers, pageRequest);

		//then
		assertThat(postLikesByIdAndFollowing).extracting("post")
			.extracting("boundary")
			.containsOnly(Boundary.ALL, Boundary.FOLLOW);
		assertThat(postLikesByIdAndFollowing).extracting("post").extracting("tempSave").containsOnly(false);
	}

	private PageRequest createPageRequest(int pageNumber, int pageSize) {
		return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLogInId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLogInId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		return user;
	}

	private void createFollow(User user, User writer) {
		em.persist(new Follow(user, writer));
	}

	private List<Post> createTestPostWithPhoto(User writer, int postCnt, int subPhotoCnt, Boundary boundary) {
		List<Photo> photos = new ArrayList<>();
		List<Post> posts = new ArrayList<>();

		for (int i = 1; i <= postCnt; i++) {
			Post post = new Post(writer, "" + i, 0L, boundary, false);
			Photo thumbnailPhoto = createThumbnailPhoto(writer, i, post);
			post.addPhoto(thumbnailPhoto);
			List<Photo> subPhotos = createSubPhotos(writer, subPhotoCnt, post);
			subPhotos.forEach(post::addPhoto);

			posts.add(post);
			photos.add(thumbnailPhoto);
			photos.addAll(subPhotos);
		}

		postRepository.saveAll(posts);

		for (Photo photo : photos) {
			em.persist(photo);
		}
		return posts;
	}

	private List<Photo> createSubPhotos(User user, int cnt, Post post) {
		List<Photo> photos = new ArrayList<>();
		for (int i = 1; i <= cnt; i++) {
			FileRequestDto fileRequestDto = new FileRequestDto("subName-" + user.getNickname() + i, 1L,
				"subUrl-" + user.getNickname() + i);
			Photo photo = new Photo(post, fileRequestDto);
			photos.add(photo);
		}
		return photos;
	}

	private Photo createThumbnailPhoto(User user, int index, Post post) {
		FileRequestDto fileRequestDto = new FileRequestDto("thumbnailName-" + user.getNickname() + index, 1L,
			"thumbnailUrl-" + user.getNickname() + index);
		return new Photo(post, fileRequestDto);
	}

	private List<PostLike> createPostLikes(List<Post> posts, User likeUser) {
		return posts.stream()
			.map(post -> new PostLike(likeUser, post))
			.collect(Collectors.toList());
	}

	private User findUserByNickname(String nickname) {
		return em.createQuery("select u from Users u where u.nickname = :nickname", User.class)
			.setParameter("nickname", nickname)
			.getSingleResult();
	}

	private List<User> findFollowingUsersByNickname(String nickname) {
		return em.createQuery(
				"select f.following from Follow f where f.follower.nickname = :nickname",
				User.class)
			.setParameter("nickname", nickname)
			.getResultList();
	}
}