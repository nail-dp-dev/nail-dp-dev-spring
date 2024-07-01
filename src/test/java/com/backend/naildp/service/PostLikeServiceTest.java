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
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class PostLikeServiceTest {

	@Autowired
	UserRepository userRepository;
	@Autowired
	PostRepository postRepository;
	@Autowired
	PostLikeRepository postLikeRepository;
	@Autowired
	PostLikeService postLikeService;
	@Autowired
	EntityManager em;

	@BeforeEach
	void setup() {
		User user = createTestMember("test@naver.com", "testUser", "0100000");
		User writer = createTestMember("writer@naver.com", "writer", "0101111");

		createTestPostAndPhoto(writer, 5);
	}

	@DisplayName("writer 가 작성한 첫번째 게시물 좋아요 테스트")
	@Test
	void likePost() {
		//given
		String userNickname = "testUser";
		String writerNickname = "writer";
		Post post = findPostByNickname(writerNickname);

		//when
		Long likePostId = postLikeService.likeByPostId(post.getId(), userNickname);

		//then
		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(userNickname);

		assertThat(postLikes).hasSize(1);
		assertThat(postLikes.get(0).getId()).isEqualTo(likePostId);
		assertThat(postLikes.get(0).getUser().getNickname()).isEqualTo(userNickname);
		assertThat(postLikes.get(0).getPost().getId()).isEqualTo(post.getId());
	}

	@DisplayName("writer 가 작성한 모든 게시물 좋아요 테스트")
	@Test
	void likeAllPostWrittenByWriter() {
		//given
		String userNickname = "testUser";
		String writerNickname = "writer";
		List<Post> postsWrittenByWriter = findAllPostsByNickname(writerNickname);

		//when
		postsWrittenByWriter.forEach(post -> postLikeService.likeByPostId(post.getId(), userNickname));

		//then
		List<PostLike> postLikesByTestUser = postLikeRepository.findAllByUserNickname(userNickname);
		List<String> postLikeUserNicknames = postLikesByTestUser.stream()
			.map(postLike -> postLike.getUser().getNickname())
			.distinct()
			.collect(Collectors.toList());

		assertThat(postLikesByTestUser).hasSize(postsWrittenByWriter.size());
		assertThat(postLikeUserNicknames).containsOnly(userNickname);
	}

	private User createTestMember(String email, String nickname, String phoneNumber) {
		SocialLogin naverLogin = new SocialLogin("NAVER", email);
		User user = new User(naverLogin, nickname, phoneNumber, "프로필url", 1000L, UserRole.USER);
		return userRepository.save(user);
	}

	private void createTestPostAndPhoto(User writer, int postCnt) {
		List<Post> postList = new ArrayList<>();
		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(writer, "content" + i, 0L, Boundary.ALL, false);
			Photo photo = new Photo(post, "thumbnailURL" + i, "thumbnailPhoto" + i);
			post.addPhoto(photo);

			em.persist(photo);
			postList.add(post);
		}
		postRepository.saveAllAndFlush(postList);
	}

	private Post findPostByNickname(String writerNickname) {
		return em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", writerNickname)
			.setMaxResults(1)
			.getSingleResult();
	}

	private List<Post> findAllPostsByNickname(String writerNickname) {
		return em.createQuery("select p from Post p where p.user.nickname = :nickname",
				Post.class)
			.setParameter("nickname", writerNickname)
			.getResultList();
	}

}