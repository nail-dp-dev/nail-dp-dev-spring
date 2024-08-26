package com.backend.naildp.repository;

import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QTag.*;
import static com.backend.naildp.entity.QTagPost.*;
import static com.backend.naildp.entity.QUser.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.config.QueryDslTestConfig;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfiguration.class, QueryDslTestConfig.class})
public class PostSearchRepositoryTest {

	@Autowired
	JPAQueryFactory queryFactory;

	@PersistenceContext
	EntityManager em;

	@Autowired
	PostRepository postRepository;

	final int POST_CNT = 10;
	String tagNameOfFollowedWriter = "가리비네일";
	String tagNameOfNotFollowedWriter = "태그아님";

	@BeforeEach
	void before() {
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writer");
		User writerNotFollowed = createUserByNickname("writerNotFollowed");

		savePostByCnt(writer, POST_CNT, Boundary.ALL, tagNameOfFollowedWriter);
		savePostByCnt(writer, POST_CNT, Boundary.FOLLOW, tagNameOfFollowedWriter);
		savePostByCnt(writer, POST_CNT, Boundary.NONE, tagNameOfFollowedWriter);

		savePostByCnt(writerNotFollowed, POST_CNT, Boundary.ALL, tagNameOfNotFollowedWriter);
		savePostByCnt(writerNotFollowed, POST_CNT, Boundary.FOLLOW, tagNameOfNotFollowedWriter);
		savePostByCnt(writerNotFollowed, POST_CNT, Boundary.NONE, tagNameOfNotFollowedWriter);

		createFollow(user, writer);

		createTag(tagNameOfFollowedWriter);
		createTag(tagNameOfNotFollowedWriter);

		em.flush();
		em.clear();
	}

	@ParameterizedTest
	@CsvSource(value = {"가리비네일, 20", "태그아님, 10"})
	void test(String keyword, int expectedPostCount) {
		String username = "writer";
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize);
		Post firstPost = em.createQuery("select p from Post p order by p.createdDate desc", Post.class)
			.setMaxResults(1)
			.getSingleResult();
		String cursor = generatedCursor(firstPost.getCreatedDate(), firstPost.getPostLikes().size(), firstPost.getId());

		List<Post> posts = queryFactory
			.select(post)
			.from(post)
			.where(
				usernamePermitted(username)
					.and(containsInPost(keyword))
					.and(lessThanCursor(cursor))
			)
			.orderBy(post.postLikes.size().desc(), post.createdDate.desc())
			.limit(pageRequest.getPageSize())
			.fetch();

		assertThat(posts).hasSize(expectedPostCount);
		assertThat(posts).extracting(Post::getBoundary).contains(Boundary.ALL);
	}

	@DisplayName("키워드를 가지는 게시물 검색")
	@ParameterizedTest
	@CsvSource(value = {"가리비네일, 20", "태그아님, 10"})
	void searchPostsByKeyword(String keyword, int expectedPostCount) {
		String username = "writer";
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize);
		Post firstPost = em.createQuery("select p from Post p order by p.createdDate desc", Post.class)
			.setMaxResults(1)
			.getSingleResult();

		Slice<Post> posts = postRepository.searchPostByKeyword(PageRequest.of(0, pageSize), keyword, username,
			firstPost.getId());

		assertThat(posts).hasSize(expectedPostCount);
		assertThat(posts).extracting(Post::getBoundary).contains(Boundary.ALL);
	}

	@DisplayName("키워드를 가지는 게시물 검색 예외 - 커서인 Post 가 삭제되었을 때")
	@ParameterizedTest
	@CsvSource(value = {"가리비네일, 20", "태그아님, 10"})
	void searchPostsByDeletedCursorId(String keyword, int expectedPostCount) {
		String username = "writer";
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize);
		Post firstPost = em.createQuery("select p from Post p order by p.createdDate desc", Post.class)
			.setMaxResults(1)
			.getSingleResult();

		assertThatThrownBy(() ->
			postRepository.searchPostByKeyword(PageRequest.of(0, pageSize), keyword, username, firstPost.getId() + 1))
			.isInstanceOf(NullPointerException.class);

		// Slice<Post> posts = postRepository.searchPostByKeyword(PageRequest.of(0, pageSize), keyword, username,
		// 	firstPost.getId() + 1);
		//
		// assertThat(posts).hasSize(expectedPostCount);
		// assertThat(posts).extracting(Post::getBoundary).contains(Boundary.ALL);
	}

	@DisplayName("아카이브에 저장한 게시물 조회")
	@Test
	void postsInArchive() {
		//given
		String nickname = "nickname";
		String writerNickname = "writer";
		User user = getUserByNickname(nickname);
		Archive archive = new Archive(user, "archive", Boundary.ALL, "default");
		em.persist(archive);

		List<Post> posts = getPublicPostsByNickname(writerNickname);

		for (Post post : posts) {
			em.persist(new ArchivePost(archive, post));
		}

		em.flush();
		em.clear();

		//when
		List<Post> result = postRepository.findPostsInArchive(nickname);

		//then
		assertThat(result).extracting(Post::getUser).extracting(User::getNickname).containsOnly(writerNickname);
		assertThat(result).hasSize(POST_CNT);
	}

	@DisplayName("아카이브가 없는 사용자의 저장한 게시물 조회")
	@Test
	void findSavedPostsWithNoArchiveUser() {
		//given
		String nickname = "nickname";
		String writerNickname = "writer";
		User user = getUserByNickname(nickname);
		List<Post> posts = getPublicPostsByNickname(writerNickname);

		em.flush();
		em.clear();

		//when
		List<Post> result = postRepository.findPostsInArchive(nickname);

		//then
		assertThat(result).hasSize(0);
	}

	@DisplayName("좋아요한 게시물 조회 - 사용자가 좋아요를 하지 않은 경우")
	@Test
	void findLikedPostsWithNoLikeUser() {
		//given
		String nickname = "nickname";
		User user = getUserByNickname(nickname);

		//when
		List<Post> likedPosts = postRepository.findLikedPosts(nickname);

		//then
		assertThat(likedPosts).hasSize(0);
	}

	@DisplayName("좋아요한 게시물 조회")
	@Test
	void findLikedPosts() {
		//given
		String nickname = "nickname";
		String writerNickname = "writer";
		User user = getUserByNickname(nickname);
		List<Post> posts = getPublicPostsByNickname(writerNickname);
		for (Post post : posts) {
			em.persist(new PostLike(user, post));
		}

		em.flush();
		em.clear();

		//when
		List<Post> likedPosts = postRepository.findLikedPosts(nickname);

		//then
		assertThat(likedPosts).hasSize(POST_CNT);
		assertThat(likedPosts).extracting(Post::getUser).extracting(User::getNickname).containsOnly(writerNickname);
	}

	private BooleanExpression usernamePermitted(String usernameCond) {

		return post.boundary.eq(Boundary.ALL)
			.or(post.boundary.eq(Boundary.FOLLOW).and(
				user.nickname.eq(usernameCond).or(
					JPAExpressions
						.select(follow)
						.from(follow)
						.where(follow.follower.nickname.eq(usernameCond)
							.and(follow.following.eq(post.user))).isNotNull()))
			);
	}

	private BooleanExpression containsInPost(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		return post.postContent.contains(keyword).or(
			JPAExpressions
				.select(tagPost.tag.name)
				.from(tagPost)
				.where(tagPost.post.eq(post))
				.contains(keyword)
		);
	}

	private BooleanExpression lessThanCursor(String cursor) {
		if (!StringUtils.hasText(cursor)) {
			return null;
		}

		DateTimeTemplate<String> stringDateTimeTemplate = Expressions.dateTimeTemplate(String.class,
			"DATE_FORMAT({0}, '%y%m%d%H%i%s')", post.createdDate);

		return StringExpressions.lpad(post.postLikes.size().stringValue(), 6, '0')
			.concat(StringExpressions.lpad(stringDateTimeTemplate.stringValue(), 12, '0')
				.concat(StringExpressions.lpad(post.id.stringValue(), 8, '0')))
			.lt(cursor);
	}

	private String generatedCursor(LocalDateTime createdDate, int postLikeCount, Long postId) {
		return String.format("%06d", postLikeCount) + createdDate.toString()
			.substring(2, 19)
			.replace("T", "")
			.replace("-", "")
			.replace(":", "")
			+ String.format("%08d", postId);
	}

	private User createUserByNickname(String nickname) {
		User user = User.builder()
			.nickname(nickname)
			.phoneNumber("pn")
			.agreement(true)
			.role(UserRole.USER)
			.build();
		em.persist(user);
		return user;
	}

	private Post createPostByUser(User user, Boundary boundary, String tagName) {
		Post post = Post.builder()
			.user(user)
			.postContent(tagName)
			.tempSave(false)
			.boundary(boundary)
			.build();

		Photo photo = new Photo(post, new FileRequestDto(".jpg", 1L, ".jpg"));
		post.addPhoto(photo);

		Tag findTag = queryFactory.selectFrom(tag).where(tag.name.eq(tagName)).fetchOne();
		TagPost tagPost = new TagPost(findTag, post);
		post.addTagPost(tagPost);

		em.persist(post);
		em.persist(photo);
		em.persist(tagPost);

		return post;
	}

	private Post savePostByCnt(User writer, int postCnt, Boundary boundary, String tagName) {
		Post lastPost = null;
		for (int i = 0; i < postCnt; i++) {
			lastPost = createPostByUser(writer, boundary, tagName);
		}
		return lastPost;
	}

	private void createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}

	private void createTag(String tagName) {
		Tag tag = new Tag(tagName);
		em.persist(tag);
	}

	private List<Post> getPublicPostsByNickname(String writerNickname) {
		return em.createQuery("select p from Post p where p.user.nickname = :nickname and p.boundary = 'ALL'",
				Post.class)
			.setParameter("nickname", writerNickname)
			.getResultList();
	}

	private User getUserByNickname(String nickname) {
		return em.createQuery("select u from Users u where u.nickname = :nickname", User.class)
			.setParameter("nickname", nickname)
			.getSingleResult();
	}
}
