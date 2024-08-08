package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.comment.CommentInfoResponse;
import com.backend.naildp.dto.comment.CommentSummaryResponse;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class CommentServiceTest {

	@Autowired
	CommentService commentService;

	@Autowired
	EntityManager em;

	final int COMMENT_CNT = 60;
	final long FIRST_CURSOR_ID = -1L;

	@BeforeEach
	void setup() {
		User postWriter = User.builder().nickname("postWriter").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		em.persist(postWriter);
		User commenter = User.builder().nickname("commenter").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		em.persist(commenter);
		User noCommentUser = User.builder()
			.nickname("noCommentUser")
			.phoneNumber("pn")
			.agreement(true)
			.role(UserRole.USER)
			.build();
		em.persist(noCommentUser);

		Post post = Post.builder().user(postWriter).postContent("content").tempSave(false).boundary(Boundary.ALL).build();
		em.persist(post);
		Post noCommentPost = Post.builder()
			.user(noCommentUser)
			.postContent("noComment")
			.tempSave(false)
			.boundary(Boundary.ALL)
			.build();
		em.persist(noCommentPost);

		for (int i = 0; i < COMMENT_CNT; i++) {
			Comment comment = new Comment(commenter, post, "comment");
			em.persist(comment);
		}
	}

	@Test
	void 댓글_첫번째_페이지_조회_테스트() {
		//given
		int pageSize = COMMENT_CNT - 1;
		Post post = em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", "postWriter")
			.getSingleResult();

		//when
		CommentSummaryResponse response = commentService.findComments(post.getId(), pageSize, FIRST_CURSOR_ID);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertNotEquals(-1L, response.getCursorId());
		assertTrue(contents.hasNext());
		assertEquals(pageSize, contents.getNumberOfElements());
		Assertions.assertThat(contents).extracting(CommentInfoResponse::getProfileUrl).containsOnly("default");
	}

	@Test
	void 댓글_조회_테스트_댓글수보다_페이지가_클때() {
		//given
		int pageSize = COMMENT_CNT + 1;
		Post post = em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", "postWriter")
			.getSingleResult();

		//when
		CommentSummaryResponse response = commentService.findComments(post.getId(), pageSize, FIRST_CURSOR_ID);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertNotEquals(-1L, response.getCursorId());
		assertFalse(contents.hasNext());
		assertNotEquals(pageSize, contents.getNumberOfElements());
		Assertions.assertThat(contents).extracting(CommentInfoResponse::getProfileUrl).containsOnly("default");
	}

	@Test
	void 댓글_조회_테스트_댓글이_없을때() {
		int pageSize = 20;
		Post post = em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", "noCommentUser")
			.getSingleResult();

		//when
		CommentSummaryResponse response = commentService.findComments(post.getId(), pageSize, FIRST_CURSOR_ID);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertEquals(-1L, response.getCursorId());
		assertFalse(contents.hasNext());
		assertEquals(0, contents.getNumberOfElements());
	}

}