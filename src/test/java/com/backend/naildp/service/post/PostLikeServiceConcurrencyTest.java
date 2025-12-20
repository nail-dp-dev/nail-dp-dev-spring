package com.backend.naildp.service.post;


import static org.assertj.core.api.Assertions.assertThat;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.IntegrationTest;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.postEntity.PostLike;
import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.repository.notification.NotificationRepository;
import com.backend.naildp.repository.post.PostLikeRepository;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@IntegrationTest
public class PostLikeServiceConcurrencyTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    PostLikeService postLikeService;

    @Autowired
    NotificationRepository notificationRepository;

    List<User> users;
    Post post;

    @BeforeEach
    void setUp() {
        User writer = createUser("writer");
        userRepository.saveAndFlush(writer);

        List<User> newUsers = createUsers(100);
        users = userRepository.saveAllAndFlush(newUsers);

        post = createPostWrittenBy(writer);
        postRepository.saveAndFlush(post);

        System.out.println("===== 데이터 저장 완료 =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== 테스트 정리 시작 =====");
        postLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        System.out.println("===== 테스트 정리 완료 =====");
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = createUser("liker" + i);
            users.add(user);
        }
        return users;
    }

    @DisplayName("좋아요 중복 요청은 하나의 레코드만 생성한다.")
    @Test
    void duplicatePostLikeResultOnlyOneLike() throws InterruptedException {
        int threadCount = 2;
        int threadPoolCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolCount);

        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    postLikeService.likeByPostId(post.getId(), users.get(0).getNickname());
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    log.error("예외 발생 : {}", e.getMessage(), e);
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        System.out.println("===== 검증 시작 =====");
        List<PostLike> postLikes = postLikeRepository.findAll();

        assertThat(postLikes).hasSize(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - successCount.get());
        System.out.println("===== 검증 완료 =====");
    }

    @DisplayName("좋아요 동시 요청 시 집계값은 틀릴 수 있다.")
    @Test
    void multiplePostLikesDoesNotThrowException() throws InterruptedException {
        int threadCount = users.size();
        int threadPoolCount = users.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolCount);

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    User user = users.get(finalI);
                    postLikeService.likeByPostId(post.getId(), user.getNickname());
                } catch (Exception e) {
                    log.error("예외 발생 : {}", e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        System.out.println("=== 검증 시작 ===");
        Post likedPost = postRepository.findById(post.getId()).orElseThrow();
        List<PostLike> postLikes = postLikeRepository.findAll();

        System.out.println("likedPost = " + likedPost.getTodayLikeCount());
        assertThat(postLikes).hasSize(users.size());
//        assertThat(likedPost.getTodayLikeCount()).isEqualTo(users.size());
        System.out.println("===== 검증 완료 =====");
    }

    @DisplayName("좋아요 동시 요청 시 집계값은 틀릴 수 있다. - completableFuture")
    @Test
    void multiplePostLikesDoesNotThrowException2() throws InterruptedException {
        int threadCount = users.size();
        int threadPoolCount = users.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolCount);

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    User user = users.get(finalI);
                    postLikeService.likePostConcurrently(post.getId(), user.getNickname());
                } catch (Exception e) {
                    log.error("예외 발생 : {}", e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        System.out.println("=== 검증 시작 ===");
        Post likedPost = postRepository.findById(post.getId()).orElseThrow();
        List<PostLike> postLikes = postLikeRepository.findAll();

        System.out.println("likedPost = " + likedPost.getTodayLikeCount());
        assertThat(postLikes).hasSize(users.size());
    }

    private User createUser(String nickname) {
        return User.builder()
                .nickname(nickname)
                .role(UserRole.USER)
                .phoneNumber("")
                .agreement(true)
                .thumbnailUrl("")
                .build();
    }

    private Post createPostWrittenBy(User writer) {
        return Post.builder()
                .user(writer)
                .postContent("")
                .sharing(0L)
                .tempSave(false)
                .boundary(Boundary.ALL)
                .build();
    }
}
