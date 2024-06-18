package com.backend.naildp;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class IntializeTest {

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp() {

        SocialLogin naverLogin = new SocialLogin("NAVER", "x@naver.com");
        User mj = new User(naverLogin, "민지", "0100000", "프로필url", 1000L, UserRole.USER);
        SocialLogin naverLogin2 = new SocialLogin("NAVER", "y@naver.com");
        User jjw = new User(naverLogin2, "정완", "0101234", "프로필url", 1000L, UserRole.USER);

        em.persist(mj);
        em.persist(jjw);

    }

    @Test
    void test() {

    }

    @Test
    void saveEntity() {
        List<User> users = em.createQuery("select u from Users u", User.class)
                .getResultList();

        User mj = users.get(0);
        User jw = users.get(1);

        Post mjPost1 = new Post(mj, "1 mj다", 0L, Boundary.ALL, false);
        Post mjPost2 = new Post(mj, "2 mj다", 0L, Boundary.ALL, false);
        Post mjPost3 = new Post(mj, "3 mj다", 0L, Boundary.ALL, false);

        em.persist(mjPost1);
        em.persist(mjPost2);
        em.persist(mjPost3);

        Comment comment1 = new Comment(jw, mjPost1, "mj다 1");
        Comment comment2 = new Comment(jw, mjPost1, "mj다 2");
        Comment comment3 = new Comment(jw, mjPost1, "mj다 3");

        em.persist(comment1);
        em.persist(comment2);
        em.persist(comment3);

        Comment mjComment = new Comment(mj, mjPost1, "민지다");
        em.persist(mjComment);

        em.flush();
        em.clear();

        List<Post> postList = em.createQuery(
                        "select p from Post p where p.user.nickname = :nickname",
                        Post.class)
                .setParameter("nickname", "민지")
                .getResultList();
        for (Post post : postList) {
            System.out.println("=================");
            System.out.println("post.getPostContent() = " + post.getPostContent());
            List<Comment> comments = em.createQuery("select c from Comment c join fetch c.user u where c.post = :post", Comment.class)
                    .setParameter("post", post)
                    .getResultList();
            for (Comment comment : comments) {
                System.out.println("comment.get = " + comment.getUser().getNickname());
                System.out.println("comment.getCommentContent() = " + comment.getCommentContent());
            }
        }
    }


}