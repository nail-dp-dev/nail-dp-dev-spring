package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
		User postWriter = createUser("postWriter");
		User commenter = createUser("commenter");
		User noCommentUser = createUser("noCommentUser");

		Post post = createPost(postWriter, "content");
		Post noCommentPost = createPost(noCommentUser, "noComment");

		for (int i = 0; i < COMMENT_CNT; i++) {
			Comment comment = createComment(commenter, post);
		}
	}

	@DisplayName("댓글 조회 테스트 - 첫번째 페이지")
	@Test
	void readCommentsInFirstPage() {
		//given
		String userNickname = "postWriter";
		int pageSize = COMMENT_CNT - 1;
		Post post = findPostByWriterNickname("postWriter");

		//when
		CommentSummaryResponse response = commentService.findComments(post.getId(), pageSize, FIRST_CURSOR_ID, userNickname);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertNotEquals(-1L, response.getCursorId());
		assertTrue(contents.hasNext());
		assertEquals(pageSize, contents.getNumberOfElements());
		assertThat(contents).extracting(CommentInfoResponse::getProfileUrl).containsOnly("default");
	}

	@DisplayName("댓글 조회 테스트 - 댓글수보다 페이지가 클때")
	@Test
	void readCommentsAbovePageElements() {
		//given
		String userNickname = "postWriter";
		int pageSize = COMMENT_CNT + 1;
		Post post = findPostByWriterNickname("postWriter");

		//when
		CommentSummaryResponse response = commentService.findComments(post.getId(), pageSize, FIRST_CURSOR_ID, userNickname);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertNotEquals(-1L, response.getCursorId());
		assertFalse(contents.hasNext());
		assertNotEquals(pageSize, contents.getNumberOfElements());
		assertThat(contents).extracting(CommentInfoResponse::getProfileUrl).containsOnly("default");
	}

	@DisplayName("댓글 조회 테스트 - 댓글이 없을때")
	@Test
	void readNoComments() {
		String userNickname = "postWriter";
		int pageSize = 20;
		Post post = findPostByWriterNickname("noCommentUser");

		//when
		CommentSummaryResponse response = commentService.findComments(post.getId(), pageSize, FIRST_CURSOR_ID, userNickname);
		Slice<CommentInfoResponse> contents = response.getContents();

		//then
		assertEquals(-1L, response.getCursorId());
		assertFalse(contents.hasNext());
		assertEquals(0, contents.getNumberOfElements());
	}

	private User createUser(String postWriter) {
		User user = User.builder().nickname(postWriter).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		em.persist(user);
		return user;
	}

	private Post createPost(User postWriter, String content) {
		Post post = Post.builder()
			.user(postWriter)
			.postContent(content)
			.tempSave(false)
			.boundary(Boundary.ALL)
			.build();
		em.persist(post);
		return post;
	}

	private Comment createComment(User commenter, Post post) {
		Comment comment = new Comment(commenter, post, "comment");
		em.persist(comment);
		return comment;
	}

	private Post findPostByWriterNickname(String postWriter) {
		return em.createQuery("select p from Post p where p.user.nickname = :nickname", Post.class)
			.setParameter("nickname", postWriter)
			.getSingleResult();
	}
}