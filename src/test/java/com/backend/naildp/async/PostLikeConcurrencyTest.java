package com.backend.naildp.async;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.service.handler.PostLikeUpdater;

@SpringBootTest
@ActiveProfiles(profiles = {"test", "secret"})
public class PostLikeConcurrencyTest {
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	UserRepository userRepository;

	@Autowired
	PostLikeRepository postLikeRepository;

	@Autowired
	PostLikeUpdater postLikeUpdater;

	private Post post;
	private List<User> users;

	@BeforeEach
	void setup() {
		User postUser = userRepository.saveAndFlush(createTestUser("postUser"));

		post = postRepository.save(Post.builder().postContent("").boundary(Boundary.ALL).user(postUser).tempSave(false).build());

		users = IntStream.range(0, 100)
			.mapToObj(i -> createTestUser("user" + i))
			.collect(Collectors.toList());
		userRepository.saveAllAndFlush(users);

		List<PostLike> postLikes = users.stream()
			.map(user -> new PostLike(user, post))
			.collect(Collectors.toList());
		postLikeRepository.saveAllAndFlush(postLikes);
	}

	@RepeatedTest(10)
	void 동시에_좋아요_100개_누르면_likeCount는_100이어야_한다() throws Exception {
		int threadCount = 100;
		int threadPoolCount = 5;
		ExecutorService service = Executors.newFixedThreadPool(threadPoolCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			int userIndex = i;
			service.execute(() -> {
				try {
					postLikeUpdater.increaseLikeCount(post.getId(), users.get(userIndex).getId());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
		assertThat(updatedPost.getTodayLikeCount()).isEqualTo(threadCount);
	}

	private static User createTestUser(String nickname) {
		return User.builder()
			.nickname(nickname)
			.phoneNumber("")
			.role(UserRole.USER)
			.agreement(true)
			.thumbnailUrl("default")
			.build();
	}

}
