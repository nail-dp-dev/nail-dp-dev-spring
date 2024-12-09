package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.service.post.PostInfoService;

import jakarta.persistence.EntityManager;

@ActiveProfiles(profiles = {"test", "secret"})
@SpringBootTest
@Transactional
public class PostInfoServiceTest {

	@Autowired
	PostInfoService postInfoService;
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

		LikeAllPosts(testUser, writer);
		System.out.println("======= BeforeEach 끝 ======");
	}

	@DisplayName("좋아요한 게시물 불러오기 테스트")
	@Test
	void findLikedPosts() {
		//given
		String nickname = "testUser";
		int pageSize = 60;

		//when
		PostSummaryResponse response = postInfoService.findLikedPost(nickname, pageSize, -1L);
		Slice<HomePostResponse> postSummaryList = (Slice<HomePostResponse>)response.getPostSummaryList();

		//then
		assertThat(postSummaryList.hasNext()).isFalse();
		assertThat(postSummaryList.getNumberOfElements()).isEqualTo(pageSize);
		assertThat(postSummaryList).extracting("like").containsOnly(true);
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		User savedUser = userRepository.save(user);
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
			FileRequestDto thumbnailFileRequestDto =
				new FileRequestDto("thumbnailPhoto" + i, 1L, "thumbnailUrl" + i);
			FileRequestDto subPhotoFileRequestDto =
				new FileRequestDto("subPhoto" + i, 1L, "subPhotoUrl" + i);

			Photo thumbnailPhoto = new Photo(post, thumbnailFileRequestDto);
			Photo subPhoto = new Photo(post, subPhotoFileRequestDto);

			post.addPhoto(thumbnailPhoto);
			post.addPhoto(subPhoto);

			em.persist(thumbnailPhoto);
			em.persist(subPhoto);
			postList.add(post);
		}
		return postRepository.saveAllAndFlush(postList);
	}

	private void LikeAllPosts(User user, User writer) {
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
