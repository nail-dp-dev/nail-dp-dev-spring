package com.backend.naildp.repository;

import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QTag.*;
import static com.backend.naildp.entity.QTagPost.*;
import static com.backend.naildp.entity.QUser.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.config.QueryDslTestConfig;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfiguration.class, QueryDslTestConfig.class})
public class TagSearchRepositoryTest {

	@Autowired
	JPAQueryFactory queryFactory;

	@Autowired
	TagPostRepository tagPostRepository;

	@PersistenceContext
	EntityManager em;

	final String WRITER_NICKNAME = "writer";
	final String USER_NICKNAME = "nickname";
	final String FOLLOWER_NICKNAME = "follower";
	final String LIKER_NICKNAME = "postLiker";

	@BeforeEach
	void before() {
		User writer = createUserByNickname(WRITER_NICKNAME);
		User follower = createUserByNickname(FOLLOWER_NICKNAME);
		User postLiker = createUserByNickname(LIKER_NICKNAME);

		em.persist(new Follow(follower, writer));

		Post post3 = createPostByUser(writer, "가리비", List.of("가리비", "가비"), false, Boundary.ALL);
		Post post4 = createPostByUser(writer, "가리비 - 팔로우 공개", List.of("가리비 네일"), false, Boundary.FOLLOW);
		Post post5 = createPostByUser(writer, "가리가", List.of("가리가"), false, Boundary.ALL);
		Post post6 = createPostByUser(writer, "가리나", List.of("가리나"), false, Boundary.ALL);
		Post post7 = createPostByUser(writer, "가리다", List.of("가리다"), false, Boundary.ALL);
		Post post8 = createPostByUser(writer, "가리라", List.of("가리라"), false, Boundary.ALL);
		Post post9 = createPostByUser(writer, "가리마", List.of("가리마"), false, Boundary.ALL);

		Post post2 = createPostByUser(writer, "가리1 - 전체공개", List.of("가리"), false, Boundary.ALL);
		Post post22 = createPostByUser(writer, "가리2 - 전체공개", List.of("가리"), false, Boundary.ALL);
		Post post23 = createPostByUser(writer, "가리3 - 전체공개", List.of("가리"), false, Boundary.ALL);
		Post post24 = createPostByUser(writer, "가리4 - 팔로우 공개", List.of("가리"), false, Boundary.FOLLOW);
		Post post25 = createPostByUser(writer, "가리 - 임시저장 + 팔로우 공개", List.of("가리"), true, Boundary.FOLLOW);
		Post post26 = createPostByUser(writer, "가리 - 비공개", List.of("가리"), false, Boundary.NONE);

		Post post1 = createPostByUser(writer, "가", List.of("가"), false, Boundary.ALL);

		Post postA = createPostByUser(writer, "A", List.of("aa"), false, Boundary.ALL);
		Post postB = createPostByUser(writer, "B", List.of("bb"), false, Boundary.ALL);
		Post postC = createPostByUser(writer, "C", List.of("cc"), false, Boundary.ALL);

		createPostByUser(writer, "", List.of("d"), false, Boundary.ALL);
		createPostByUser(writer, "", List.of("e"), false, Boundary.ALL);

		createPostByUser(writer, "", List.of("de"), false, Boundary.ALL);
		createPostByUser(writer, "", List.of("df"), false, Boundary.ALL);
		createPostByUser(writer, "", List.of("eg"), false, Boundary.ALL);
		createPostByUser(writer, "", List.of("eh"), false, Boundary.ALL);

		createPostByUser(writer, "", List.of("ad"), false, Boundary.ALL);
		createPostByUser(writer, "", List.of("ae"), false, Boundary.ALL);

		for (int i = 0; i < 1; i++) {
			em.persist(new PostLike(postLiker, post1));
		}

		for (int i = 0; i < 1; i++) {
			em.persist(new PostLike(postLiker, post2));
		}

		for (int i = 0; i < 2; i++) {
			em.persist(new PostLike(postLiker, post22));
		}

		for (int i = 0; i < 3; i++) {
			em.persist(new PostLike(postLiker, post23));
		}

		for (int i = 0; i < 4; i++) {
			em.persist(new PostLike(postLiker, post24));
		}

		for (int i = 0; i < 5; i++) {
			em.persist(new PostLike(postLiker, post26));
		}

		em.flush();
		em.clear();
	}

	@DisplayName("키워드 리스트를 포함하는 태그 이름 조회 테스트")
	@Test
	void findTagsContainingAnyOfKeyword() {
		//given
		List<String> keywords = List.of("d", "e");

		BooleanBuilder builder = new BooleanBuilder();
		for (String keyword : keywords) {
			builder.or(tag.name.startsWithIgnoreCase(keyword));
		}

		CaseBuilder caseBuilder = new CaseBuilder();
		NumberExpression<Integer> startWithAnyKeyword = caseBuilder.when(builder).then(1)
			.otherwise(0);

		//when
		List<TagPost> tagPosts = queryFactory
			.select(tagPost)
			.from(tagPost)
			.join(tagPost.post, post).fetchJoin().join(tagPost.tag, tag).fetchJoin()
			.where(post.tempSave.isFalse()
				.and(usernamePermitted(USER_NICKNAME))
				.and(keywordContainedInTag(keywords))
			)
			.orderBy(tag.name.length().asc(), startWithAnyKeyword.desc(), tag.name.asc())
			.fetch();
		List<String> tagNames = tagPosts.stream().map(TagPost::getTag).map(Tag::getName).toList();

		//then
		for (String tagName : tagNames) {
			System.out.println("tagName = " + tagName);
		}
		Assertions.assertThat(tagNames).allSatisfy(tagName -> {
			// keywords 의 요소 중 하나라도 tagName 에 포함되는지 확인
			Assertions.assertThat(keywords).anySatisfy(keyword -> Assertions.assertThat(tagName).contains(keyword));
		});

	}

	@DisplayName("키워드로 연관 태그 검색시 오름차순으로 10개까지 정렬")
	@Test
	void searchRelatedTagsContainingKeyword() {
		//given
		String keyword = "가";

		//when
		List<TagPost> tagPosts = tagPostRepository.searchRelatedTags(List.of(keyword), FOLLOWER_NICKNAME);

		//then
		assertThat(tagPosts).extracting(TagPost::getTag).extracting(Tag::getName).startsWith(keyword);
		assertThat(tagPosts).extracting(TagPost::getTag)
			.extracting(Tag::getName)
			.isSortedAccordingTo(Comparator.comparingLong(String::length).thenComparing(String::compareTo));
		assertThat(tagPosts).extracting(TagPost::getPost)
			.extracting(Post::getBoundary)
			.containsAnyOf(Boundary.ALL, Boundary.FOLLOW);
	}

	@DisplayName("키워드로 연관 태그 검색시 결과가 없을 때")
	@Test
	void searchRelatedTagsNoResult() {
		//given
		String keyword = "가나다라마바사아";

		//when
		List<TagPost> tagPosts = tagPostRepository.searchRelatedTags(List.of(keyword), USER_NICKNAME);

		//then
		assertThat(tagPosts).hasSize(0);
	}

	@DisplayName("여러개의 키워드로 연관 태그 검색")
	@Test
	void searchRelatedTagsWithNumberOfTags() {
		//given
		List<String> keywords = List.of("aa", "bb");

		//when
		List<TagPost> tagPosts = tagPostRepository.searchRelatedTags(keywords, USER_NICKNAME);

		//then
		assertThat(tagPosts).hasSize(keywords.size());
		assertThat(tagPosts).extracting(TagPost::getTag).extracting(Tag::getName).containsExactlyElementsOf(keywords);
	}

	private BooleanBuilder keywordContainedInTag(List<String> keywords) {
		if (keywords.isEmpty()) {
			return null;
		}

		BooleanBuilder builder = new BooleanBuilder();

		keywords.forEach(keyword -> builder.or(tag.name.containsIgnoreCase(keyword)));

		return builder;
	}

	private BooleanExpression usernamePermitted(String usernameCond) {
		return post.boundary.eq(Boundary.ALL)
			.or(post.boundary.eq(Boundary.FOLLOW)
				.and(user.nickname.eq(usernameCond).or(isFollowerOfPostWriter(usernameCond)))
			);
	}

	private BooleanExpression isFollowerOfPostWriter(String usernameCond) {
		return JPAExpressions
			.select(follow)
			.from(follow)
			.where(follow.follower.nickname.eq(usernameCond)
				.and(follow.following.eq(post.user))).isNotNull();
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

	private Post createPostByUser(User user, String postContent, List<String> tagNames, boolean tempSave,
		Boundary boundary) {

		Post post = Post.builder()
			.user(user)
			.postContent(postContent)
			.tempSave(tempSave)
			.boundary(boundary)
			.build();
		em.persist(post);

		savePhoto(tagNames, post);

		for (String tagName : tagNames) {
			Tag tag = ifPresentGetOrSave(tagName);
			saveTagPost(post, tag);
		}

		return post;
	}

	private void savePhoto(List<String> tagNames, Post post) {
		Photo photo = new Photo(post,
			new FileRequestDto("photoName" + (tagNames.size() > 0 ? tagNames.get(0) : ""), 1L, "photoUrl"));
		post.addPhoto(photo);
		em.persist(photo);
	}

	private void saveTagPost(Post post, Tag tag) {
		TagPost tagPost = new TagPost(tag, post);
		post.addTagPost(tagPost);
		em.persist(tagPost);
	}

	private Tag ifPresentGetOrSave(String tagName) {
		List<Tag> tags = em.createQuery("select t from Tag t where t.name = :name", Tag.class)
			.setParameter("name", tagName)
			.getResultList();

		if (tags.isEmpty()) {
			Tag tag = new Tag(tagName);
			em.persist(tag);
			return tag;
		}

		return tags.get(0);
	}

}
