package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class PostServiceTest {

	@Autowired
	PostService postService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	SocialLoginRepository socialLoginRepository;
	@Autowired
	PostRepository postRepository;
	@Autowired
	PostLikeRepository postLikeRepository;
	@Autowired
	ArchivePostRepository archivePostRepository;
	@Autowired
	FollowRepository followRepository;
	@Autowired
	EntityManager em;

	private static final int POST_CNT = 30;

	@BeforeEach
	void setup() {
		System.out.println("======= BeforeEach 시작 ======");

		User testUser = createTestMember("testUser@naver.com", "testUser", "0100000", 1L);
		User writer = createTestMember("writer@naver.com", "writer", "0101111", 2L);

		followRepository.save(new Follow(testUser, writer));

		Archive archive = createTestArchive(testUser, "publicArchive", Boundary.ALL);

		List<Post> postsToPublic = createTestPostAndPhoto(writer, POST_CNT, Boundary.ALL);
		List<Post> postsToFollow = createTestPostAndPhoto(writer, POST_CNT, Boundary.FOLLOW);
		List<Post> postsToNone = createTestPostAndPhoto(writer, POST_CNT, Boundary.NONE);

		savePostsInArchive(archive, postsToPublic);
		savePostsInArchive(archive, postsToFollow);
		savePostsInArchive(archive, postsToNone);

		createTestPostLikes(testUser, writer);
		System.out.println("======= BeforeEach 끝 ======");
	}

	@DisplayName("최신 게시글 조회 메서드 연속 호출 테스트")
	@Test
	void callNewPostsTwice() {
		//given
		int firstCallPageSize = 30;
		int secondCallPageSize = 40;

		String nickname = "testUser";

		//when
		PostSummaryResponse firstPostSummaryResponse = postService.homePosts("NEW", firstCallPageSize, -1L, nickname);
		Long oldestPostId = firstPostSummaryResponse.getOldestPostId();
		System.out.println("oldestPostId = " + oldestPostId);
		PostSummaryResponse secondPostSummaryResponse = postService.homePosts("NEW", secondCallPageSize, oldestPostId,
			nickname);

		Slice<HomePostResponse> firstSummaryList = firstPostSummaryResponse.getPostSummaryList();
		Slice<HomePostResponse> secondSummaryList = secondPostSummaryResponse.getPostSummaryList();

		//then
		assertThat(firstSummaryList.hasNext()).isTrue();
		assertThat(firstSummaryList.getSize()).isEqualTo(firstCallPageSize);
		assertThat(firstSummaryList.getNumberOfElements()).isEqualTo(firstCallPageSize);

		assertThat(secondSummaryList.hasNext()).isFalse();
		assertThat(secondSummaryList.getSize()).isEqualTo(secondCallPageSize);
		assertThat(secondSummaryList.getNumberOfElements()).isEqualTo(60 - firstCallPageSize);
	}

	@DisplayName("최신 게시물 불러오기 테스트")
	@Test
	void findNewPosts() {
		//given
		String nickname = "testUser";
		int postCnt = 60;
		int pageNumber = 0;
		int pageSize = 20;
		long cursorPostId = -1L;
		int totalPages = postCnt / pageSize + (postCnt % pageSize == 0 ? 0 : 1);

		//when
		System.out.println("첫 페이지");
		PostSummaryResponse postSummaryResponse = postService.homePosts("NEW", pageSize, cursorPostId, nickname);
		Slice<HomePostResponse> responses = postSummaryResponse.getPostSummaryList();

		List<Boolean> savedList = responses.stream().map(HomePostResponse::getSaved).toList();
		List<Boolean> likedList = responses.stream().map(HomePostResponse::getLike).toList();

		//then
		assertThat(responses.getSize()).isEqualTo(pageSize);
		assertThat(responses.getNumber()).isEqualTo(pageNumber);
		assertThat(responses.hasNext()).isTrue();

		assertThat(savedList).containsOnly(true);
		assertThat(savedList).hasSize(pageSize);

		assertThat(likedList).contains(true);
		assertThat(likedList).hasSize(pageSize);
	}

	@DisplayName("좋아요한 게시물 불러오기 테스트")
	@Test
	void findLikedPosts() {
		//given
		String nickname = "testUser";
		int postCnt = 60;
		int pageNumber = 0;
		int pageSize = 20;
		int totalPages = postCnt / pageSize + (postCnt % pageSize == 0 ? 0 : 1);

		//when
		Page<HomePostResponse> responses = postService.findLikedPost(nickname, pageNumber);
		List<Boolean> savedList = responses.stream().map(HomePostResponse::getSaved).toList();
		List<Boolean> likedList = responses.stream().map(HomePostResponse::getLike).toList();

		//then
		assertThat(responses.getSize()).isEqualTo(pageSize);
		assertThat(responses.getNumber()).isEqualTo(pageNumber);
		assertThat(responses.getTotalElements()).isEqualTo(postCnt);
		assertThat(responses.getTotalPages()).isEqualTo(totalPages);

		assertThat(savedList).containsOnly(true);
		assertThat(savedList).hasSize(pageSize);

		assertThat(likedList).contains(true);
		assertThat(likedList).hasSize(pageSize);
	}

	@Test
	void test() {
		//given
		String nickname = "writer";
		User user = em.createQuery("select u from Users u where u.nickname = :nickname", User.class)
			.setParameter("nickname", nickname)
			.getSingleResult();
		PostRequestDto postRequestDto = new PostRequestDto("testContent", false, Boundary.ALL, new ArrayList<>());
		postService.uploadPost(nickname, postRequestDto, new ArrayList<>());
		Post post = em.createQuery("select p from Post p where p.postContent = 'testContent'", Post.class)
			.getSingleResult();
		for (int i = 1; i <= 10; i++) {
			em.persist(new Comment(user, post, "comment"));
		}
		em.flush();
		em.clear();

		//when

		//then
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialId, "kakao", email, user);
		User savedUser = userRepository.save(user);
		socialLoginRepository.save(socialLogin);
		return savedUser;
	}

	private Archive createTestArchive(User testUser, String archiveName, Boundary boundary) {
		Archive archive = new Archive(testUser, archiveName, boundary);
		em.persist(archive);
		return archive;
	}

	private List<Post> createTestPostAndPhoto(User writer, int postCnt, Boundary boundary) {
		List<Post> postList = new ArrayList<>();
		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(writer, "content" + i, 0L, boundary, false);
			Photo thumbnailPhoto = new Photo(post, "thumbnailURL" + i, "thumbnailPhoto" + i);
			Photo subPhoto = new Photo(post, "subPhotoURL" + i, "subPhoto" + i);

			post.addPhoto(thumbnailPhoto);
			post.addPhoto(subPhoto);

			em.persist(thumbnailPhoto);
			em.persist(subPhoto);
			postList.add(post);
		}
		return postRepository.saveAllAndFlush(postList);
	}

	private void createTestPostLikes(User user, User writer) {
		List<Post> posts = em.createQuery("select p from Post p where p.user = :user", Post.class)
			.setParameter("user", writer)
			.getResultList();
		List<PostLike> postLikes = posts.stream().map(post -> new PostLike(user, post)).collect(Collectors.toList());
		postLikeRepository.saveAllAndFlush(postLikes);
	}

	private void savePostsInArchive(Archive archive, List<Post> posts) {
		List<ArchivePost> archivePosts = posts.stream()
			.map(post -> new ArchivePost(archive, post))
			.collect(Collectors.toList());
		archivePostRepository.saveAllAndFlush(archivePosts);
	}
}
