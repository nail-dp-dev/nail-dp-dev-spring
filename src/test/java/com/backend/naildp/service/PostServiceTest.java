package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.ProfileType;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.post.PostInfoResponse;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UsersProfile;
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
		User writer = createTestMember("writer@naver.com", "writer", "0101111", 2L);

		createPostByCntAndBoundary(writer, 10, Boundary.ALL);

		em.flush();
		em.clear();
	}

	@DisplayName("특정 게시물 상세 조회 테스트")
	@Test
	void postInfoTest() {
		//given
		String userNickname = "writer";
		Post post = em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", userNickname)
			.setMaxResults(1)
			.getSingleResult();

		//when
		PostInfoResponse postInfoResponse = postService.postInfo(userNickname, post.getId());

		//then
		assertThat(postInfoResponse).extracting(PostInfoResponse::getNickname).isEqualTo(userNickname);
		assertThat(postInfoResponse).extracting(PostInfoResponse::getProfileUrl).isEqualTo(userNickname + "Url");
	}

	@DisplayName("팔로우한 유저의 게시물 상세 조회 테스트")
	@Test
	void posInfoWithFollow() {
		//given
		User user = userRepository.findByNickname("user").orElseThrow();
		User writer = userRepository.findByNickname("writer").orElseThrow();
		em.persist(new Follow(user, writer));
		Post post = em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", writer.getNickname())
			.setMaxResults(1)
			.getSingleResult();

		//when
		PostInfoResponse postInfoResponse = postService.postInfo(user.getNickname(), post.getId());

		//then
		assertThat(postInfoResponse).extracting(PostInfoResponse::getNickname).isEqualTo(writer.getNickname());
		assertThat(postInfoResponse).extracting(PostInfoResponse::getProfileUrl)
			.isEqualTo(writer.getNickname() + "Url");
		assertThat(postInfoResponse).extracting(PostInfoResponse::getFollowerCount).isEqualTo(1L);
		assertThat(postInfoResponse.isFollowingStatus()).isTrue();
	}

	@DisplayName("좋아요한 게시물 상세 조회 테스트")
	@Test
	void postInfoWithLikeTest() {
		//given
		String writerNickname = "writer";
		User user = userRepository.findByNickname("user").orElseThrow();
		Post post = em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", writerNickname)
			.setMaxResults(1)
			.getSingleResult();
		PostLike postLike = new PostLike(user, post);
		post.addPostLike(postLike);
		em.persist(postLike);

		//when
		PostInfoResponse postInfoResponse = postService.postInfo(user.getNickname(), post.getId());

		//then
		assertThat(postInfoResponse).extracting(PostInfoResponse::getNickname).isEqualTo(writerNickname);
		assertThat(postInfoResponse).extracting(PostInfoResponse::getProfileUrl).isEqualTo(writerNickname + "Url");
		assertThat(postInfoResponse).extracting(PostInfoResponse::getLikeCount).isEqualTo(1L);
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		em.persist(user);

		addSocialLoginInfo(email, socialId, user);
		addProfile(user);

		return user;
	}

	private void addProfile(User user) {
		Profile profile = Profile.builder()
			.name(user.getNickname() + "Profile")
			.profileUrl(user.getNickname() + "Url")
			.thumbnail(true)
			.profileType(ProfileType.BASIC)
			.build();
		UsersProfile usersProfile = UsersProfile.builder().profile(profile).user(user).build();

		em.persist(profile);
		em.persist(usersProfile);
	}

	private void addSocialLoginInfo(String email, Long socialId, User user) {
		SocialLogin socialLogin = new SocialLogin(socialId, "kakao", email, user);

		em.persist(socialLogin);
	}

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
}
