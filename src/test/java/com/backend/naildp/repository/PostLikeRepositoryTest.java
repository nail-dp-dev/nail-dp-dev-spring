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
import org.springframework.data.domain.Sort;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
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
		User user1 = createTestMember("mj@naver.com", "mj", "0100000", 1L);
		User user2 = createTestMember("jw@naver.com", "jw", "0109999", 2L);
		User writer1 = createTestMember("writer1@naver.com", "writer1", "0101111", 3L);
		User writer2 = createTestMember("writer2@naver.com", "writer2", "0102222", 4L);
		User writer3 = createTestMember("writer3@naver.com", "writer3", "0103333", 5L);

		// 게시물 생성
		List<Post> writerPosts1 = createTestPostWithPhoto(writer1, 5, 5);
		List<Post> writerPosts2 = createTestPostWithPhoto(writer2, 6, 6);
		List<Post> writerPosts3 = createTestPostWithPhoto(writer3, 7, 7);

		List<PostLike> postLikesByMj = writerPosts1.stream()
			.map(post -> new PostLike(user1, post))
			.collect(Collectors.toList());
		List<PostLike> postLikesByJw1 = writerPosts2.stream()
			.map(post -> new PostLike(user2, post))
			.collect(Collectors.toList());
		List<PostLike> postLikesByJw2 = writerPosts3.stream()
			.map(post -> new PostLike(user2, post))
			.collect(Collectors.toList());

		postLikeRepository.saveAllAndFlush(postLikesByMj);
		postLikeRepository.saveAllAndFlush(postLikesByJw1);
		postLikeRepository.saveAllAndFlush(postLikesByJw2);

		em.clear();
	}

	@DisplayName("한명의 게시물을 좋아요한 유저 검증 테스트")
	@Test
	void validatePostLikeInfo() {
		//given
		String nickname = "mj";

		//when
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);

		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).toList();

		List<String> writerNicknameList = likedPosts.stream()
			.map(Post::getUser)
			.map(User::getNickname)
			.distinct()
			.toList();

		List<String> likeUserNicknameList = postLikes.stream()
			.map(PostLike::getUser)
			.map(User::getNickname)
			.distinct()
			.toList();

		//then
		assertThat(likedPosts.size()).isEqualTo(5);
		likedPosts.forEach(post -> assertThat(post.getPhotos().size()).isEqualTo(6));

		assertThat(writerNicknameList.size()).isEqualTo(1);
		assertThat(writerNicknameList).containsOnly("writer1");
		assertThat(likeUserNicknameList.size()).isEqualTo(1);
		assertThat(likeUserNicknameList).containsOnly(nickname);

	}

	@DisplayName("두명의 게시물을 좋아요한 유저 검증 테스트")
	@Test
	void validatePostLikeByUser2() {
		//given
		String nickname = "jw";

		//when
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);

		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).toList();
		List<Post> likedPostsByWriter2 = postLikes.stream()
			.map(PostLike::getPost)
			.filter(post -> post.getUser().getNickname().equals("writer2"))
			.toList();
		List<Post> likedPostsByWriter3 = postLikes.stream()
			.map(PostLike::getPost)
			.filter(post -> post.getUser().getNickname().equals("writer3"))
			.toList();

		List<String> writerNicknameList = likedPosts.stream()
			.map(Post::getUser)
			.map(User::getNickname)
			.distinct()
			.toList();

		List<String> likeUserNicknameList = postLikes.stream()
			.map(PostLike::getUser)
			.map(User::getNickname)
			.distinct()
			.toList();

		//then
		assertThat(likedPosts.size()).isEqualTo(likedPostsByWriter2.size() + likedPostsByWriter3.size());

		likedPostsByWriter2.forEach(post -> assertThat(post.getPhotos().size()).isEqualTo(7));
		likedPostsByWriter3.forEach(post -> assertThat(post.getPhotos().size()).isEqualTo(8));

		assertThat(writerNicknameList.size()).isEqualTo(2);
		assertThat(writerNicknameList).containsOnly("writer2", "writer3");
		assertThat(likeUserNicknameList.size()).isEqualTo(1);
		assertThat(likeUserNicknameList).containsOnly(nickname);
	}

	@DisplayName("사용자 mj 가 좋아요한 모든 게시물 좋아요 취소 테스트")
	@Test
	void deletePostLike() {
		//given
		String nickname = "mj";
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
		String nickname = "mj";
		PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

		//when
		Page<PostLike> pagedPostLikesByBoundaryNotNone = postLikeRepository.findPagedPostLikesByBoundaryOpened(
			pageRequest, nickname, Boundary.NONE);
		Page<PostLike> pagedPostLikesByBoundaryNotFollowed = postLikeRepository.findPagedPostLikesByBoundaryOpened(
			pageRequest, nickname, Boundary.FOLLOW);
		Page<PostLike> pagedPostLikesByBoundaryNotAll = postLikeRepository.findPagedPostLikesByBoundaryOpened(
			pageRequest, nickname, Boundary.ALL);

		//then
		assertThat(pagedPostLikesByBoundaryNotNone).hasSize(5);
		assertThat(pagedPostLikesByBoundaryNotFollowed).hasSize(5);
		assertThat(pagedPostLikesByBoundaryNotAll).hasSize(0);

		assertThat(pagedPostLikesByBoundaryNotNone.getNumber()).isEqualTo(0);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getNumber()).isEqualTo(0);
		assertThat(pagedPostLikesByBoundaryNotAll.getNumber()).isEqualTo(0);

		assertThat(pagedPostLikesByBoundaryNotNone.getSize()).isEqualTo(20);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getSize()).isEqualTo(20);
		assertThat(pagedPostLikesByBoundaryNotAll.getSize()).isEqualTo(20);

		assertThat(pagedPostLikesByBoundaryNotNone.getTotalElements()).isEqualTo(5);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getTotalElements()).isEqualTo(5);
		assertThat(pagedPostLikesByBoundaryNotAll.getTotalElements()).isEqualTo(0);

		assertThat(pagedPostLikesByBoundaryNotNone.getTotalPages()).isEqualTo(1);
		assertThat(pagedPostLikesByBoundaryNotFollowed.getTotalPages()).isEqualTo(1);
		assertThat(pagedPostLikesByBoundaryNotAll.getTotalPages()).isEqualTo(0);

	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLogInId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLogInId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		return user;
	}

	private List<Post> createTestPostWithPhoto(User user, int postCnt, int subPhotoCnt) {
		List<Photo> photos = new ArrayList<>();
		List<Post> posts = new ArrayList<>();

		for (int i = 1; i <= postCnt; i++) {
			Post post = new Post(user, "" + i, 0L, Boundary.ALL, false);
			Photo thumbnailPhoto = createThumbnailPhoto(user, i, post);
			post.addPhoto(thumbnailPhoto);
			List<Photo> subPhotos = createSubPhotos(user, subPhotoCnt, post);
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
			Photo photo = new Photo(post, "subUrl-" + user.getNickname() + i, "subName-" + user.getNickname() + i);
			photos.add(photo);
		}
		return photos;
	}

	private Photo createThumbnailPhoto(User user, int index, Post post) {
		return new Photo(post, "thumbnailUrl-" + user.getNickname() + index,
			"thumbnailName-" + user.getNickname() + index);
	}

}