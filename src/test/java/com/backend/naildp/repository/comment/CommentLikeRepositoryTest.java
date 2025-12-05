package com.backend.naildp.repository.comment;

import static org.assertj.core.api.Assertions.*;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.TestDatabaseConfig;
import com.backend.naildp.config.TestEnvConfig;
import com.backend.naildp.config.TestRedisConfig;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.postEntity.PostLike;
import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.repository.post.PostLikeRepository;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({TestDatabaseConfig.class, TestRedisConfig.class, TestEnvConfig.class})
class CommentLikeRepositoryTest {

    @Autowired
    PostLikeRepository postLikeRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;

    User writer;
    User reader;

    Post post;

    @BeforeEach
    void setUp() {
        writer = createUserAndSave("writer");
        reader = createUserAndSave("reader");

        post = createPostAndSave(writer);
        createPostLikeAndSave(reader, post);
    }

    @Test
    void saveDuplicateEntity() {
        // given
        User reader = userRepository.findById(this.reader.getId()).orElseThrow(IllegalStateException::new);
        Post post = postRepository.findById(this.post.getId()).orElseThrow(IllegalStateException::new);

        // when & then
        PostLike postLike = new PostLike(reader, post);

        assertThatThrownBy(() -> postLikeRepository.saveAndFlush(postLike))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User createUserAndSave(String nickname) {
        User user = User.builder()
                .nickname(nickname)
                .thumbnailUrl("")
                .agreement(false)
                .phoneNumber("")
                .role(UserRole.USER)
                .build();
        return userRepository.saveAndFlush(user);
    }

    private Post createPostAndSave(User user) {
        Post post = Post.builder()
                .user(user)
                .sharing(0L)
                .postContent("")
                .boundary(Boundary.ALL)
                .tempSave(false)
                .build();
        return postRepository.saveAndFlush(post);
    }

    private void createPostLikeAndSave(User user, Post post) {
        PostLike postLike = new PostLike(reader, post);
        postLikeRepository.saveAndFlush(postLike);
    }

}