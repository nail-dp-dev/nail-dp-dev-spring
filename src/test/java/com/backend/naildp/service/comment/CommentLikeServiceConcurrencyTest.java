package com.backend.naildp.service.comment;

import static org.assertj.core.api.Assertions.*;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.IntegrationTest;
import com.backend.naildp.entity.commentEntity.Comment;
import com.backend.naildp.entity.commentEntity.CommentLike;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.repository.comment.CommentLikeRepository;
import com.backend.naildp.repository.comment.CommentRepository;
import com.backend.naildp.repository.notification.NotificationRepository;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.repository.user.UserRepository;
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
class CommentLikeServiceConcurrencyTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private NotificationRepository notificationRepository;

    User commentLiker;
    Post post;
    Comment comment;


    @BeforeEach
    void setUp() {
        User writer = createUser("writer");
        User commenter = createUser("commenter");
        commentLiker = createUser("liker");

        userRepository.saveAllAndFlush(List.of(writer, commenter, commentLiker));

        post = createPost(writer);
        comment = createComment(commenter, post, "test comment");
        post.addComment(comment);

        postRepository.saveAndFlush(post);
        commentRepository.saveAndFlush(comment);
    }

    @AfterEach
    void tearDown() {
        commentLikeRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("중복된 좋아요 요청을 해도 좋아요 수는 1개다.")
    @Test
    void saveDuplicateEntity() throws InterruptedException {
        // when
        int threadCount = 5;
        int threadPoolCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    commentLikeService.likeComment(post.getId(), comment.getId(), commentLiker.getNickname());
                } catch(Exception e) {
                    log.info("test failed : {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        //then
        List<CommentLike> commentLikes = commentLikeRepository.findAll();
        log.info("commentLikes.size() : {}", commentLikes.size());

        // unique constraints 적용 전
//        assertThat(commentLikes).hasSizeGreaterThan(1);
//        assertThat(commentLikes).hasSize(threadCount);

        // unique constraints 적용 후
        assertThat(commentLikes).hasSize(1);
    }

    @DisplayName("동시에 5번 댓글 좋아요를 하면 좋아요수가 더 생길 수 있다.")
    @Test
    void saveDuplicateEntityAndCatchException() throws InterruptedException {
        // when
        int threadCount = 5;
        int threadPoolCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolCount);

        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    commentLikeService.likeCommentConcurrently(post.getId(), comment.getId(), commentLiker.getNickname());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.info("test failed : {}", e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        //then
        List<CommentLike> commentLikes = commentLikeRepository.findAll();

        assertThat(commentLikes).hasSize(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(4);
    }


    @Test
    void likeCommentAgain() {
        // given
        User commentLiker = userRepository.findById(this.commentLiker.getId()).orElseThrow(IllegalStateException::new);
        Comment comment = commentRepository.findById(this.comment.getId()).orElseThrow(IllegalStateException::new);
        commentLikeRepository.saveAndFlush(new CommentLike(commentLiker, comment));

        // when & then
        commentLikeService.likeCommentConcurrently(post.getId(), comment.getId(), commentLiker.getNickname());
//        assertThatThrownBy(() -> commentLikeService.likeCommentIfEmpty(post.getId(), comment.getId(), commentLiker.getNickname()))
//                .isInstanceOf(UnexpectedRollbackException.class)
//                .hasMessage("Transaction silently rolled back because it has been marked as rollback-only");
        assertThat(commentLikeRepository.findAll()).hasSize(1);
    }

    private static User createUser(String username) {
        return User.builder()
                .nickname(username)
                .agreement(true)
                .phoneNumber("phoneNumber")
                .thumbnailUrl("thumbnail")
                .role(UserRole.USER)
                .build();

    }

    private static Post createPost(User writer) {
        return Post.builder()
                .boundary(Boundary.ALL)
                .postContent("")
                .tempSave(false)
                .user(writer)
                .sharing(0L)
                .build();
    }

    private static Comment createComment(User commenter, Post post, String commentString) {
        return Comment.of(commenter, post, commentString);
    }

}