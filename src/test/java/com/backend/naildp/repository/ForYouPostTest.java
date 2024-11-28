package com.backend.naildp.repository;

import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QPostLike.*;
import static com.backend.naildp.entity.QTagPost.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.config.QueryDslTestConfig;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.QPost;
import com.backend.naildp.entity.QPostLike;
import com.backend.naildp.entity.QTagPost;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles(profiles = "test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfiguration.class, QueryDslTestConfig.class})
public class ForYouPostTest {

	@Autowired
	JPAQueryFactory queryFactory;

	@PersistenceContext
	EntityManager em;

	final int POST_CNT = 1;

	@BeforeEach
	void before() {
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writer");

		Tag tag1 = createTag("태그1");
		Tag tag2 = createTag("태그2");
		Tag tag3 = createTag("태그3");
		Tag tag4 = createTag("태그4");
		Tag tag5 = createTag("태그5");

		Post postContainingAllTags = createPostByUserAndBoundaryAndTag(writer, Boundary.ALL,
			List.of(tag1, tag2, tag3, tag4, tag5));
		Post postContainingTag1 = createPostByUserAndBoundaryAndTag(writer, Boundary.ALL, List.of(tag1));
		Post postContainingTag2 = createPostByUserAndBoundaryAndTag(writer, Boundary.ALL, List.of(tag2));
		Post postContainingTag3 = createPostByUserAndBoundaryAndTag(writer, Boundary.ALL, List.of(tag3));
		Post postContainingTag4 = createPostByUserAndBoundaryAndTag(writer, Boundary.ALL, List.of(tag4));
		Post postContainingTag5 = createPostByUserAndBoundaryAndTag(writer, Boundary.ALL, List.of(tag5));

		em.persist(new PostLike(user, postContainingAllTags));

		em.flush();
		em.clear();
	}

	@Test
	void forYouPostTest() {
		//given
		String username = "nickname";

		//when
		List<Post> likedPost = queryFactory
			.select(post)
			.from(postLike).join(postLike.post, post)
			.where(postLike.user.nickname.eq(username))
			.fetch();

		List<Long> tagIdsInLikedPost = queryFactory
			.select(tagPost.tag.id)
			.from(tagPost)
			.where(tagPost.post.in(likedPost))
			.distinct()
			.fetch();

		List<Post> forYouPosts = queryFactory
			.select(tagPost.post)
			.from(tagPost)
			.where(tagPost.tag.id.in(tagIdsInLikedPost).and(tagPost.post.notIn(likedPost)))
			.fetch();

		//then
		assertThat(tagIdsInLikedPost).hasSize(5);
		assertThat(forYouPosts).hasSize(5);

		System.out.println("===============");
		List<String> tagNames = forYouPosts.stream()
			.map(Post::getTagPosts)
			.flatMap(tagPosts -> tagPosts.stream().map(tagPost1 -> tagPost1.getTag().getName()))
			.toList();

		assertThat(tagNames).contains("태그1", "태그2", "태그3", "태그4", "태그5");
	}

	private User createUserByNickname(String nickname) {
		User user = User.builder()
			.nickname(nickname)
			.phoneNumber("pn")
			.agreement(true)
			.thumbnailUrl("")
			.role(UserRole.USER)
			.build();
		em.persist(user);
		return user;
	}

	private Post createPostByUserAndBoundaryAndTag(User user, Boundary boundary, List<Tag> tags) {
		Post post = Post.builder()
			.user(user)
			.postContent(tags.get(0).getName())
			.tempSave(false)
			.boundary(boundary)
			.build();

		Photo photo = new Photo(post, new FileRequestDto(".jpg", 1L, ".jpg"));
		post.addPhoto(photo);

		for (Tag tag : tags) {
			TagPost tagPost = new TagPost(tag, post);
			post.addTagPost(tagPost);
			em.persist(tagPost);
		}

		em.persist(post);
		em.persist(photo);

		return post;
	}

	private void savePostByCnt(User writer, int postCnt, Boundary boundary, List<Tag> tags) {
		for (int i = 0; i < postCnt; i++) {
			createPostByUserAndBoundaryAndTag(writer, boundary, tags);
		}
	}

	private Tag createTag(String tagName) {
		Tag tag = new Tag(tagName);
		em.persist(tag);
		return tag;
	}
}
