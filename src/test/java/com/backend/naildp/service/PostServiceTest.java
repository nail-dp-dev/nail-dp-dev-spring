package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.ProfileType;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.post.PostBoundaryRequest;
import com.backend.naildp.dto.post.PostInfoResponse;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UsersProfile;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class PostServiceTest {

	@Autowired
	PostService postService;
	@Autowired
	PostRepository postRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	FollowRepository followRepository;
	@Autowired
	EntityManager em;

	@BeforeEach
	void setup() {
		User user = createTestMember("user@naver.com", "user", "0100000", 1L);
		user.thumbnailUrlUpdate("userURL");
		User writer = createTestMember("writer@naver.com", "writer", "0101111", 2L);
		writer.thumbnailUrlUpdate("writerUrl");

		createPostByCntAndBoundary(writer, 10, Boundary.ALL);
		createPostByCntAndBoundary(writer, 10, Boundary.FOLLOW);
		createPostByCntAndBoundary(writer, 10, Boundary.NONE);

		em.flush();
		em.clear();
	}

	@DisplayName("특정 게시물 상세 조회 테스트")
	@Test
	void postInfoTest() {
		//given
		String userNickname = "writer";
		Boundary boundary = Boundary.ALL;
		Post post = getFirstPostByNicknameAndBoundary(userNickname, boundary);
		User user = em.createQuery("select u from Users u where u.nickname = :nickname", User.class)
			.setParameter("nickname", "user")
			.getSingleResult();
		PostLike postLike = new PostLike(user, post);
		em.persist(postLike);

		em.flush();
		em.clear();

		//when
		PostInfoResponse postInfoResponse = postService.postInfo(userNickname, post.getId());

		//then
		assertThat(postInfoResponse).extracting(PostInfoResponse::getNickname).isEqualTo(userNickname);
		assertThat(postInfoResponse).extracting(PostInfoResponse::getProfileUrl).isEqualTo(userNickname + "Url");
		assertThat(postInfoResponse).extracting(PostInfoResponse::getBoundary).isEqualTo(boundary.toString());
	}

	@DisplayName("팔로우한 유저의 게시물 상세 조회 테스트")
	@ParameterizedTest
	@EnumSource(value = Boundary.class, names = {"ALL", "FOLLOW"})
	void posInfoWithFollow(Boundary boundary) {
		//given
		User user = userRepository.findByNickname("user").orElseThrow();
		User writer = userRepository.findByNickname("writer").orElseThrow();
		em.persist(new Follow(user, writer));
		Post post = getFirstPostByNicknameAndBoundary(writer.getNickname(), boundary);

		//when
		PostInfoResponse postInfoResponse = postService.postInfo(user.getNickname(), post.getId());

		//then
		assertThat(postInfoResponse).extracting(PostInfoResponse::getNickname).isEqualTo(writer.getNickname());
		assertThat(postInfoResponse).extracting(PostInfoResponse::getProfileUrl)
			.isEqualTo(writer.getNickname() + "Url");
		assertThat(postInfoResponse).extracting(PostInfoResponse::getFollowerCount).isEqualTo(1L);
		assertThat(postInfoResponse.isFollowingStatus()).isTrue();
		assertThat(postInfoResponse).extracting(PostInfoResponse::getBoundary).isEqualTo(boundary.toString());
	}

	@DisplayName("좋아요한 게시물 상세 조회 테스트")
	@Test
	void postInfoWithLikeTest() {
		//given
		String writerNickname = "writer";
		User user = userRepository.findByNickname("user").orElseThrow();
		Post publicPost = getFirstPostByNicknameAndBoundary(writerNickname, Boundary.ALL);
		PostLike postLike = new PostLike(user, publicPost);
		publicPost.addPostLike(postLike);
		em.persist(postLike);

		//when
		PostInfoResponse postInfoResponse = postService.postInfo(user.getNickname(), publicPost.getId());

		//then
		assertThat(postInfoResponse).extracting(PostInfoResponse::getNickname).isEqualTo(writerNickname);
		assertThat(postInfoResponse).extracting(PostInfoResponse::getProfileUrl).isEqualTo(writerNickname + "Url");
		assertThat(postInfoResponse).extracting(PostInfoResponse::getLikeCount).isEqualTo(1L);
		assertThat(postInfoResponse).extracting(PostInfoResponse::getBoundary)
			.isEqualTo(publicPost.getBoundary().toString());
	}

	@DisplayName("게시물 공개 범위 설정 시 예외 테스트 - 작성자와 요청자가 일치하지 않을 때")
	@Test
	void changeBoundaryExceptionBecauseOfNotWrittenUser() {
		//given
		String userNickname = "writer";
		Post post = getFirstPostByNicknameAndBoundary(userNickname, Boundary.ALL);
		PostBoundaryRequest postBoundaryRequest = new PostBoundaryRequest(Boundary.NONE);
		String wrongUserNickname = "wrongUser";

		//when & then
		assertThatThrownBy(
			() -> postService.changeBoundary(post.getId(), postBoundaryRequest, wrongUserNickname))
			.isInstanceOf(CustomException.class)
			.extracting(Throwable::getMessage).isEqualTo("게시글 범위 설정은 작성자만 할 수 있습니다.");
	}

	@DisplayName("게시물 공개 범위 설정 테스트")
	@ParameterizedTest
	@EnumSource(Boundary.class)
	void changeBoundary(Boundary boundary) {
		//given
		String userNickname = "writer";
		Post post = getFirstPostByNicknameAndBoundary(userNickname, Boundary.ALL);
		PostBoundaryRequest postBoundaryRequest = new PostBoundaryRequest(boundary);

		//when
		postService.changeBoundary(post.getId(), postBoundaryRequest, userNickname);

		//then
		assertThat(post.getBoundary()).isEqualTo(boundary);
	}

	@DisplayName("게시물 공유 횟수 조회 예외 테스트 - 비공개 게시물인 경우")
	@Test
	void privatePostSharedCountException() {
		//given
		Post privatePost = getFirstPostByNicknameAndBoundary("writer", Boundary.NONE);

		//when & then
		assertThatThrownBy(() -> postService.countSharing(privatePost.getId(), "user"))
			.isInstanceOf(CustomException.class)
			.hasMessage("비공개 게시물은 작성자만 접근할 수 있습니다.");
	}

	@DisplayName("게시물 공유 횟수 조회 예외 테스트 - 팔로우 공개 게시물인데 팔로우가 아닐 때")
	@Test
	void followPostSharedCountException() {
		//given
		String notFollowNickname = "notFollower";
		Post followPost = getFirstPostByNicknameAndBoundary("writer", Boundary.FOLLOW);

		//when & then
		assertThatThrownBy(() -> postService.countSharing(followPost.getId(), notFollowNickname))
			.isInstanceOf(CustomException.class)
			.hasMessage("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.");
	}

	@DisplayName("게시물 공유 횟수 조회 테스트")
	@Test
	void postSharedCount() {
		//given
		Post post = getFirstPostByNicknameAndBoundary("writer", Boundary.ALL);

		//when
		Long sharedCount = postService.countSharing(post.getId(), "writer");

		//then
		assertThat(sharedCount).isEqualTo(0L);
	}

	@DisplayName("게시물 공유 예외 - 임시 저장 게시물 공유")
	@Test
	void shareTempSavedPostException() {
		//given
		User writer = userRepository.findByNickname("writer").orElseThrow();
		Post tempSavedPost = Post.builder()
			.user(writer)
			.postContent("content")
			.boundary(Boundary.ALL)
			.tempSave(true)
			.build();

		em.persist(tempSavedPost);

		//when & then
		assertThatThrownBy(() -> postService.sharePost(tempSavedPost.getId(), writer.getNickname()))
			.isInstanceOf(CustomException.class)
			.hasMessage("임시저장한 게시물은 공유할 수 없습니다.");
	}

	@DisplayName("게시물 공유 테스트")
	@Test
	void sharePost() {
		//given
		User writer = userRepository.findByNickname("writer").orElseThrow();
		Post post = Post.builder().user(writer).postContent("content").boundary(Boundary.ALL).tempSave(false).build();
		postRepository.saveAndFlush(post);

		//when
		Long sharedPostId = postService.sharePost(post.getId(), writer.getNickname());
		em.flush();
		em.clear();

		Post findPost = postRepository.findById(post.getId()).orElseThrow();

		//then
		assertThat(findPost.getSharing()).isEqualTo(1);
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		em.persist(user);

		// addSocialLoginInfo(email, socialId, user);
		addProfile(user);

		return user;
	}

	private void addProfile(User user) {
		Profile profile = Profile.builder()
			.name(user.getNickname() + "Profile")
			.profileUrl(user.getNickname() + "Url")
			.profileType(ProfileType.BASIC)
			.build();
		UsersProfile usersProfile = UsersProfile.builder().profile(profile).user(user).build();

		em.persist(profile);
		em.persist(usersProfile);
	}

	// private void addSocialLoginInfo(String email, Long socialId, User user) {
	// 	SocialLogin socialLogin = new SocialLogin(socialId, "kakao", email, user);
	//
	// 	em.persist(socialLogin);
	// }

	private List<Post> createPostByCntAndBoundary(User writer, int postCnt, Boundary boundary) {
		List<Post> postList = new ArrayList<>();
		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(writer, "content", 0L, boundary, false);
			postList.add(post);

			FileRequestDto thumbnailFileRequestDto =
				new FileRequestDto("thumbnailPhoto.jpg", 1L, "thumbnailUrl");
			FileRequestDto subPhotoFileRequestDto =
				new FileRequestDto("subPhoto.jpg", 1L, "subPhotoUrl");

			addPhotoInPost(post, thumbnailFileRequestDto);
			addPhotoInPost(post, subPhotoFileRequestDto);

			createTagByPost(i, post);
		}
		return postRepository.saveAllAndFlush(postList);
	}

	private void addPhotoInPost(Post post, FileRequestDto fileRequestDto) {
		Photo thumbnailPhoto = new Photo(post, fileRequestDto);
		post.addPhoto(thumbnailPhoto);
		em.persist(thumbnailPhoto);
	}

	private void createTagByPost(int i, Post post) {
		Tag tag = new Tag("tag" + i);
		TagPost tagPost = new TagPost(tag, post);

		post.addTagPost(tagPost);

		em.persist(tag);
		em.persist(tagPost);
	}

	private Post getFirstPostByNicknameAndBoundary(String userNickname, Boundary boundary) {
		return em.createQuery("select p from Post p where p.user.nickname = :nickname and p.boundary = :boundary",
				Post.class)
			.setParameter("nickname", userNickname)
			.setParameter("boundary", boundary)
			.setMaxResults(1)
			.getSingleResult();
	}
}
