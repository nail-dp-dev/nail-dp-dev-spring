package com.backend.naildp.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class CommentLikeServiceTest {

	@Autowired
	EntityManager em;
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

		Post publicPost = createPost(postWriter, "전체공개", Boundary.ALL);
		Post followPost = createPost(postWriter, "팔로우공개", Boundary.FOLLOW);
		Post privatePost = createPost(postWriter, "비공개공개", Boundary.NONE);

		publicPostId = publicPost.getId();
		privatePostId = privatePost.getId();
		followPostId =followPost.getId();

		for (int i = 0; i < 10; i++) {
			Comment comment = new Comment(followerUser, publicPost, "" + i);
			em.persist(comment);
		}
		

		em.flush();
		em.clear();
	}

	@Test
	void test() {
		//given
		Post post = postRepository.findPostAndUser(followPostId).orElseThrow();
		

		//when
		List<Comment> comments = post.getComments();

		for (Comment comment : comments) {
			System.out.println("comment.getCommentContent() = " + comment.getCommentContent());
		}
		//then

	}

	private Follow getFollow(User followerUser, User postWriter) {
		Follow follow = new Follow(followerUser, postWriter);
		em.persist(follow);
		return follow;
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
}
