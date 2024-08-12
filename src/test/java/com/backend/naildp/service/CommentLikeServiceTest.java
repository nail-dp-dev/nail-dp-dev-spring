package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class CommentLikeServiceTest {

	@Autowired
	EntityManager em;
	@Autowired
	CommentLikeService commentLikeService;
	@Autowired
	PostRepository postRepository;
	@Autowired
	FollowRepository followRepository;

	Long publicPostId;
	Long followPostId;
	Long privatePostId;

	@BeforeEach
	void setup() {
		User notFollower = createUser("notFollower");
		User followerUser = createUser("follower");
		User postWriter = createUser("postWriter");

		saveFollow(followerUser, postWriter);

		Post publicPost = createPost(postWriter, "전체공개", Boundary.ALL);
		Post followPost = createPost(postWriter, "팔로우공개", Boundary.FOLLOW);
		Post privatePost = createPost(postWriter, "비공개공개", Boundary.NONE);

		publicPostId = publicPost.getId();
		privatePostId = privatePost.getId();
		followPostId =followPost.getId();

		for (int i = 0; i < 10; i++) {
			Comment followComment = new Comment(followerUser, followPost, "" + i);
			em.persist(followComment);
			Comment privateComment = new Comment(followerUser, privatePost, "" + i);
			em.persist(privateComment);
			Comment publicComment = new Comment(followerUser, publicPost, "" + i);
			em.persist(publicComment);
		}

		em.flush();
		em.clear();
	}

	@DisplayName("작성자가 아닌 사용자가 비공개 게시물 조회 시 예외 발생")
	@Test
	void likeCommentOfPrivatePostByNotPostWriterException() {
		//given
		Post post = postRepository.findPostAndUser(privatePostId).orElseThrow();

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentLikeService.likeComment(post.getId(), privatePostId, "notFollower"));

		//then
		assertEquals("비공개 게시물은 작성자만 접근할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.INVALID_BOUNDARY, exception.getErrorCode());
	}

	@DisplayName("팔로워가 아닌 사용자가 비공개 게시물 조회 시 예외 발생")
	@Test
	void likeCommentOfFollowPostsByNotFollowerException() {
		//given
		Post post = postRepository.findPostAndUser(followPostId).orElseThrow();
		Comment comment = findOneFromPost(post);

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> commentLikeService.likeComment(post.getId(), comment.getId(), "notFollower"));

		//then
		assertEquals("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", exception.getMessage());
		assertEquals(ErrorCode.INVALID_BOUNDARY, exception.getErrorCode());
	}

	@DisplayName("작성자가 팔로우 공개 게시물의 댓글 좋아요 테스트")
	@Test
	void likeCommentOfFollowPostsByPostWriter() {
		//given
		Post post = postRepository.findPostAndUser(followPostId).orElseThrow();
		User writer = post.getUser();
		Comment comment = findOneFromPost(post);

		//when
		Long commentLikeId = commentLikeService.likeComment(post.getId(), comment.getId(), writer.getNickname());

		//then
		assertThat(comment.getCommentLikes()).hasSize(1);
	}

	@DisplayName("팔로워가 팔로우 공개 게시물의 댓글 좋아요 테스트")
	@Test
	void likeCommentOfFollowPostsByFollower() {
		//given
		Post post = postRepository.findPostAndUser(followPostId).orElseThrow();
		String followerNickname = "follower";
		Comment comment = findOneFromPost(post);

		//when
		Long commentLikeId = commentLikeService.likeComment(post.getId(), comment.getId(), followerNickname);

		//then
		assertThat(comment.getCommentLikes()).hasSize(1);
	}

	@DisplayName("전체 공개 게시물의 댓글 좋아요 테스트")
	@ParameterizedTest
	@ValueSource(strings = {"follower", "notFollower", "postWriter"})
	void likeCommentOfPublicPosts(String accessNickname) {
		//given
		Post post = postRepository.findPostAndUser(publicPostId).orElseThrow();
		Comment comment = findOneFromPost(post);

		//when
		Long commentLikeId = commentLikeService.likeComment(post.getId(), comment.getId(), accessNickname);

		//then
		assertThat(comment.getCommentLikes()).hasSize(1);
	}

	@DisplayName("댓글 좋아요 취소 테스트")
	@Test
	void cancelCommentLike() {
		//given
		Post post = postRepository.findPostAndUser(publicPostId).orElseThrow();
		User writer = post.getUser();
		Comment comment = findOneFromPost(post);
		em.persist(new CommentLike(writer, comment));

		em.flush();
		em.clear();

		//when
		commentLikeService.cancelCommentLike(post.getId(), comment.getId(), writer.getNickname());

		//then
		Long commentLikeCnt = em.createQuery("select count(cl) from CommentLike cl where cl.comment = :comment", Long.class)
			.setParameter("comment", comment)
			.getSingleResult();
		assertEquals(0, commentLikeCnt);
	}

	private Post createPost(User postWriter, String postContent, Boundary boundary) {
		Post post = Post.builder()
			.user(postWriter)
			.postContent(postContent)
			.tempSave(false)
			.boundary(boundary)
			.build();
		em.persist(post);
		return post;
	}

	private User createUser(String nickname) {
		User user = User.builder().nickname(nickname).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		em.persist(user);
		return user;
	}

	private Follow saveFollow(User followerUser, User postWriter) {
		Follow follow = new Follow(followerUser, postWriter);
		em.persist(follow);
		return follow;
	}

	private Comment findOneFromPost(Post post) {
		Comment comment = em.createQuery("select c from Comment c where c.post = :post", Comment.class)
			.setParameter("post", post)
			.setMaxResults(1)
			.getSingleResult();
		return comment;
	}
}
