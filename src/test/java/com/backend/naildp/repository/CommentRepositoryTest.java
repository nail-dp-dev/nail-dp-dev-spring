package com.backend.naildp.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@Import({JpaAuditingConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

	@Autowired
	CommentRepository commentRepository;
	@Autowired
	EntityManager em;

	@Test
	void 댓글_좋아요_개수_세기() {
		//given
		User user = User.builder().nickname("nickname").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		User commentLiker = User.builder()
			.nickname("commentLiker")
			.phoneNumber("pn")
			.agreement(true)
			.role(UserRole.USER)
			.build();
		Post post = Post.builder().user(user).postContent("content").boundary(Boundary.ALL).tempSave(false).build();
		Comment comment = new Comment(user, post, "comment");

		em.persist(user);
		em.persist(commentLiker);
		em.persist(post);
		em.persist(comment);

		int likeCnt = 5;
		for (int i = 0; i < likeCnt; i++) {
			em.persist(new CommentLike(commentLiker, comment));
		}

		em.flush();
		em.clear();

		//when
		long commentLikeCount = commentRepository.countLikesById(comment.getId());

		//then
		assertEquals(likeCnt, commentLikeCount);
	}
}