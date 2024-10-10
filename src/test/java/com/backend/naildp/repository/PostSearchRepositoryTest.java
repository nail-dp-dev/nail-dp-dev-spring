package com.backend.naildp.repository;

import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QPostLike.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

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
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.jsonwebtoken.lang.Strings;
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

	@BeforeEach
	void before() {
		User user = createUserByNickname("nickname");
		User writer = createUserByNickname("writer");
		User writerNotFollowed = createUserByNickname("writerNotFollowed");
		User writerUsingTags = createUserByNickname("writerUsingTags");

		Tag tag = createTag("가리비네일");
		Tag notTag = createTag("태그아님");
		Tag springNailTag = createTag("봄 네일");
		Tag summerNailTag = createTag("여름 네일");

		savePostByCnt(writer, POST_CNT, Boundary.ALL, List.of(tag));
		savePostByCnt(writer, POST_CNT, Boundary.FOLLOW, List.of(tag));
		savePostByCnt(writer, POST_CNT, Boundary.NONE, List.of(tag));

		savePostByCnt(writerNotFollowed, POST_CNT, Boundary.ALL, List.of(notTag));
		savePostByCnt(writerNotFollowed, POST_CNT, Boundary.FOLLOW, List.of(notTag));
		savePostByCnt(writerNotFollowed, POST_CNT, Boundary.NONE, List.of(notTag));

		savePostByCnt(writerUsingTags, POST_CNT, Boundary.ALL, List.of(springNailTag, summerNailTag));
		savePostByCnt(writerUsingTags, POST_CNT, Boundary.FOLLOW, List.of(springNailTag, summerNailTag));

		createFollow(user, writer);

		em.flush();
		em.clear();
	}

	@DisplayName("키워드를 가지는 게시물 검색")
	@ParameterizedTest
	@CsvSource(value = {"가리비네일, 20", "태그아님, 10"})
	void searchPostsByKeyword(String keyword, int expectedPostCount) {
		//given
		String username = "writer";
		int pageSize = 20;
		Post firstPost = em.createQuery("select p from Post p order by p.createdDate desc", Post.class)
			.setMaxResults(1)
			.getSingleResult();

		//when
		Slice<Post> posts = postRepository.searchPostByKeyword(PageRequest.of(0, pageSize), List.of(keyword), username,
			firstPost.getId());

		//then
		assertThat(posts.hasNext()).isFalse();
		assertThat(posts).hasSize(expectedPostCount);
		assertThat(posts.getNumberOfElements()).isEqualTo(expectedPostCount);
		assertThat(posts).extracting(Post::getBoundary).contains(Boundary.ALL);
	}

	@DisplayName("키워드를 가지는 게시물 검색 예외 - 커서인 Post 가 삭제되었을 때")
	@ParameterizedTest
	@CsvSource(value = {"가리비네일, 20", "태그아님, 10"})
	void searchPostsByDeletedCursorId(String keyword, int expectedPostCount) {
		//given
		String username = "writer";
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize);
		Post firstPost = em.createQuery("select p from Post p order by p.createdDate desc", Post.class)
			.setMaxResults(1)
			.getSingleResult();

		//when & then
		assertThatThrownBy(() ->
			postRepository.searchPostByKeyword(PageRequest.of(0, pageSize), List.of(keyword), username,
				firstPost.getId() + 1))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("일반 사용자로 두개의 키워드를 갖는 게시물 검색")
	@Test
	void searchPostsContainingTwoKeywordsByNormalUser() {
		//given
		String username = "user";
		int pageSize = 30;
		PageRequest pageRequest = PageRequest.of(0, pageSize);

		//when
		Slice<Post> posts = postRepository.searchPostByKeyword(pageRequest,
			List.of("봄 네일", "여름 네일"),
			username, null);
		List<String> tags = posts.flatMap(p -> p.getTagPosts().stream().map(tp -> tp.getTag().getName())).toList();

		//then
		assertThat(posts).hasSize(POST_CNT);
		assertThat(tags).contains("봄 네일", "여름 네일");
	}

	@DisplayName("작성자로 두개의 키워드를 갖는 게시물 검색")
	@Test
	void searchPostsContainingTwoKeywordsByPostWriter() {
		//given
		String username = "writerUsingTags";
		int pageSize = 30;
		PageRequest pageRequest = PageRequest.of(0, pageSize);

		//when
		Slice<Post> posts = postRepository.searchPostByKeyword(pageRequest,
			List.of("봄 네일", "여름 네일"),
			username, null);
		List<String> tags = posts.flatMap(p -> p.getTagPosts().stream().map(tp -> tp.getTag().getName())).toList();

		//then
		assertThat(posts).hasSize(POST_CNT * 2);
		assertThat(tags).contains("봄 네일", "여름 네일");

	}

	@DisplayName("게시물 내용에 없는 키워드로 검색")
	@Test
	void searchNotContainedKeyword() {
		//given
		String username = "writer";
		String keyword = "xxxx";
		int pageSize = 20;

		//when
		Slice<Post> posts = postRepository.searchPostByKeyword(PageRequest.of(0, pageSize), List.of(keyword), username,
			null);

		//then
		assertThat(posts).hasSize(0);
		assertThat(posts.hasNext()).isFalse();
		assertThat(posts.getNumberOfElements()).isEqualTo(0);
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

	@DisplayName("회원이 NEW 게시물을 처음으로 조회")
	@Test
	void userFindNewestPostSlice() {
		//given
		String username = "nickname";
		Long cursorPostId = null;
		int pageSize = 40;
		PageRequest pageRequest = PageRequest.of(0, pageSize);

		//when
		Slice<Post> newestPostSlice = postRepository.findNewestPostSlice(username, cursorPostId, pageRequest);

		//then
		assertThat(newestPostSlice).extracting(Post::getBoundary).contains(Boundary.ALL, Boundary.FOLLOW);
		assertThat(newestPostSlice).extracting(Post::getUser)
			.extracting(User::getNickname)
			.contains("writer", "writerNotFollowed", "writerUsingTags");
		assertThat(newestPostSlice).extracting(Post::getTempSave).containsOnly(false);
		assertThat(newestPostSlice.hasNext()).isFalse();
		assertThat(newestPostSlice).hasSize(pageSize);
	}

	@DisplayName("회원이 NEW 게시물 조회 - 커서 기반 페이지네이션")
	@Test
	void userFindNewestPostSliceWithCursorPost() {
		//given
		String username = "nickname";
		int pageSize = 40;
		PageRequest pageRequest = PageRequest.of(0, pageSize);
		Post newestPost = queryFactory
			.selectFrom(post)
			.where(post.tempSave.isFalse().and(post.boundary.eq(Boundary.ALL)))
			.orderBy(post.createdDate.desc())
			.limit(1)
			.fetchOne();
		em.clear();

		//when
		Slice<Post> newestPostSliceByCursor = postRepository.findNewestPostSlice(username, newestPost.getId(),
			pageRequest);

		//then
		assertThat(newestPostSliceByCursor).hasSize(pageSize - 1);
		assertThat(newestPostSliceByCursor.hasNext()).isFalse();

		assertThat(newestPostSliceByCursor).extracting(Post::getBoundary).contains(Boundary.ALL, Boundary.FOLLOW);
		assertThat(newestPostSliceByCursor).extracting(Post::getUser)
			.extracting(User::getNickname)
			.contains("writer", "writerNotFollowed", "writerUsingTags");
		assertThat(newestPostSliceByCursor).extracting(Post::getTempSave).containsOnly(false);
	}

	@DisplayName("회원이 아닌 사용자가 최신 게시물 조회시 전체 공개인 게시물만 조회할 수 있다.")
	@Test
	void anonymousFindNewestPost() {
		//given
		String username = null;
		Long cursorPostId = null;
		int pageSize = 30;
		PageRequest pageRequest = PageRequest.of(0, pageSize);

		//when
		Slice<Post> newestPostSlice = postRepository.findNewestPostSlice(username, cursorPostId, pageRequest);

		//then
		assertThat(newestPostSlice).extracting(Post::getBoundary).contains(Boundary.ALL);
		assertThat(newestPostSlice).extracting(Post::getUser)
			.extracting(User::getNickname)
			.contains("writer", "writerNotFollowed", "writerUsingTags");
		assertThat(newestPostSlice).extracting(Post::getTempSave).containsOnly(false);
		assertThat(newestPostSlice.hasNext()).isFalse();
		assertThat(newestPostSlice).hasSize(pageSize);
	}

	@DisplayName("회원이 아닌 사용자가 최신 게시물 조회시 전체 공개인 게시물만 조회할 수 있다. - 커서 기반 페이징")
	@Test
	void anonymousFindNewestPostWithCursorPost() {
		//given
		String username = null;
		Post newestPost = queryFactory
			.selectFrom(post)
			.where(post.tempSave.isFalse().and(post.boundary.eq(Boundary.ALL)))
			.orderBy(post.createdDate.desc())
			.limit(1)
			.fetchOne();
		Long cursorPostId = newestPost.getId();
		int pageSize = 30;
		PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

		em.clear();

		//when
		Slice<Post> newestPostSlice = postRepository.findNewestPostSlice(username, cursorPostId, pageRequest);

		//then
		assertThat(newestPostSlice).hasSize(pageSize - 1);
		assertThat(newestPostSlice.hasNext()).isFalse();

		assertThat(newestPostSlice).extracting(Post::getBoundary).contains(Boundary.ALL);
		assertThat(newestPostSlice).extracting(Post::getUser)
			.extracting(User::getNickname)
			.contains("writer", "writerNotFollowed", "writerUsingTags");
		assertThat(newestPostSlice).extracting(Post::getTempSave).containsOnly(false);
	}

	@DisplayName("자정부터 현시각까지 좋아요수 많은 순서대로 게시물 조회")
	@Test
	void trendPost() {
		//given
		User liker = getUserByNickname("nickname");
		List<Post> publicPostFromWriter = findPostByNicknameAndBoundary("writer", Boundary.ALL);
		addPostLike(liker, publicPostFromWriter, 3);
		List<Post> followPostFromWriter = findPostByNicknameAndBoundary("writer", Boundary.FOLLOW);
		addPostLike(liker, followPostFromWriter, 2);
		List<Post> publicPostFromWriterNotFollowed = findPostByNicknameAndBoundary("writerNotFollowed", Boundary.ALL);
		addPostLike(liker, publicPostFromWriterNotFollowed, 1);

		em.flush();
		em.clear();

		String username = "nickname";
		int pageSize = 40;
		Long cursorPostId = null;
		PageRequest pageRequest = PageRequest.of(0, pageSize);

		//when
		// Slice<Post> postSlice = findTrendPostSliceV2(username, cursorPostId, pageRequest);
		Slice<Post> postSlice = postRepository.findTrendPostSlice(username, cursorPostId, pageRequest);

		//then
		assertThat(postSlice).hasSize(pageSize);
		assertThat(postSlice.hasNext()).isFalse();
		assertThat(postSlice).extracting(Post::getTempSave).containsOnly(false);
		assertThat(postSlice.getContent()).isSortedAccordingTo(
			Comparator.comparingInt((Post post) -> (int)post.getPostLikes()
					.stream()
					.filter(postLike1 -> postLike1.getCreatedDate().isBefore(LocalDateTime.now()) &&
						postLike1.getCreatedDate().isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
					.count()).reversed()
				.thenComparing(Post::getCreatedDate, Comparator.reverseOrder())
		);
	}

	@DisplayName("자정부터 현시각까지 좋아요수 많은 순서대로 게시물 조회 - 커서 페이징")
	@Test
	void trendPostWithCursor() {
		//given
		User liker = getUserByNickname("nickname");
		List<Post> publicPostFromWriter = findPostByNicknameAndBoundary("writer", Boundary.ALL);
		addPostLike(liker, publicPostFromWriter, 3);
		List<Post> followPostFromWriter = findPostByNicknameAndBoundary("writer", Boundary.FOLLOW);
		addPostLike(liker, followPostFromWriter, 2);
		List<Post> publicPostFromWriterNotFollowed = findPostByNicknameAndBoundary("writerNotFollowed", Boundary.ALL);
		addPostLike(liker, publicPostFromWriterNotFollowed, 1);

		em.flush();
		em.clear();

		String username = "nickname";
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize);

		//when
		// Slice<Post> trendPostSliceWithoutCursor = findTrendPostSliceV2(username, null, pageRequest);
		Slice<Post> trendPostSliceWithoutCursor = postRepository.findTrendPostSlice(username, null, pageRequest);
		Post cursorPost = trendPostSliceWithoutCursor.getContent()
			.get(trendPostSliceWithoutCursor.getNumberOfElements() - 1);
		// Slice<Post> trendPostSliceWithCursor = findTrendPostSliceV2(username, cursorPost.getId(), pageRequest);
		Slice<Post> trendPostSliceWithCursor = postRepository.findTrendPostSlice(username, cursorPost.getId(), pageRequest);

		//then
		assertThat(trendPostSliceWithCursor).hasSize(pageSize);
		assertThat(trendPostSliceWithCursor.hasNext()).isFalse();
		assertThat(trendPostSliceWithCursor).extracting(Post::getTempSave).containsOnly(false);
		assertThat(trendPostSliceWithCursor.getContent()).isSortedAccordingTo(
			Comparator.comparingInt((Post post) -> (int)post.getPostLikes()
					.stream()
					.filter(postLike1 -> postLike1.getCreatedDate().isBefore(LocalDateTime.now()) &&
						postLike1.getCreatedDate().isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
					.count()).reversed()
				.thenComparing(Post::getCreatedDate, Comparator.reverseOrder())
		);
	}

	/**
	 * 자정부터 현시각까지 좋아요가 많은 게시물 조회
	 */
	private Slice<Post> findTrendPostSlice(String username, Long cursorPostId, PageRequest pageRequest) {
		NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");

		List<Tuple> tuples = queryFactory
			.select(post,
				ExpressionUtils.as(JPAExpressions
						.select(postLike.count())
						.from(postLike)
						.where(postLike.post.eq(post),
							postLike.createdDate
								.between(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT), LocalDateTime.now())),
					likeCount)
			)
			.from(post)
			.where(post.tempSave.isFalse()
				.and(isAllowedToViewPosts(username))
				.and(
					hasLessLikeThanCursorPost(cursorPostId)
				)
			)
			.orderBy(likeCount.desc(), post.createdDate.desc())
			.limit(pageRequest.getPageSize() + 1)
			.fetch();

		List<Post> posts = tuples.stream().map(tuple -> tuple.get(post)).collect(Collectors.toList());

		return new SliceImpl<>(posts, pageRequest, hasNext(posts, pageRequest.getPageSize()));
	}

	private Slice<Post> findTrendPostSliceV2(String username, Long cursorPostId, PageRequest pageRequest) {
		JPQLQuery<Long> postLikeCountQuery = JPAExpressions
			.select(postLike.count())
			.from(postLike)
			.where(postLike.post.eq(post),
				postLike.createdDate
					.between(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT), LocalDateTime.now()));

		OrderSpecifier<Long> orderSpecifier = new OrderSpecifier<>(Order.DESC, postLikeCountQuery);

		List<Post> posts = queryFactory
			.select(post)
			.from(post)
			.where(post.tempSave.isFalse()
				.and(isAllowedToViewPosts(username))
				.and(hasLessLikeThanCursorPost(cursorPostId))
			)
			.orderBy(orderSpecifier, post.createdDate.desc())
			.limit(pageRequest.getPageSize() + 1)
			.fetch();

		return new SliceImpl<>(posts, pageRequest, hasNext(posts, pageRequest.getPageSize()));
	}

	private BooleanExpression hasLessLikeThanCursorPost(Long cursorPostId) {
		if (cursorPostId == null) {
			return null;
		}

		JPQLQuery<Long> postTodayLikeQuery = JPAExpressions
			.select(postLike.count())
			.from(postLike)
			.where(postLike.post.eq(post),
				postLike.createdDate.between(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT),
					LocalDateTime.now()));

		JPQLQuery<Long> cursorPostTodayLikeQuery = JPAExpressions
			.select(postLike.count())
			.from(postLike)
			.where(postLike.post.id.eq(cursorPostId)
				, postLike.createdDate.between(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT),
					LocalDateTime.now()));

		return postTodayLikeQuery.lt(cursorPostTodayLikeQuery)
			.or(postTodayLikeQuery.eq(cursorPostId).and(isRegisteredBeforeCursorPost(cursorPostId)));
	}

	private BooleanExpression isRegisteredBeforeCursorPost(Long cursorPostId) {
		if (cursorPostId == null) {
			return null;
		}

		return post.createdDate.before(
			JPAExpressions.select(post.createdDate).from(post).where(post.id.eq(cursorPostId)));
	}

	private boolean hasNext(List<Post> posts, int size) {
		if (posts.size() > size) {
			posts.remove(size);
			return true;
		}
		return false;
	}

	private void addPostLike(User liker, List<Post> publicPostFromWriter, double likeCount) {
		publicPostFromWriter.forEach(post -> {
			for (int i = 0; i < likeCount; i++) {
				em.persist(new PostLike(liker, post));
			}
		});
	}

	private List<Post> findPostByNicknameAndBoundary(String writerNickname, Boundary boundary) {
		return queryFactory.selectFrom(post)
			.where(post.user.nickname.eq(writerNickname).and(post.boundary.eq(boundary)))
			.fetch();
	}

	private BooleanExpression isAllowedToViewPosts(String usernameCond) {
		if (!Strings.hasText(usernameCond)) {
			return post.boundary.eq(Boundary.ALL);
		}

		return post.boundary.eq(Boundary.ALL)
			.or(post.boundary.eq(Boundary.FOLLOW).and(
				post.user.nickname.eq(usernameCond).or(
					JPAExpressions
						.select(follow)
						.from(follow)
						.where(follow.follower.nickname.eq(usernameCond)
							.and(follow.following.eq(post.user))).isNotNull()))
			);
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

	private void createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}

	private Tag createTag(String tagName) {
		Tag tag = new Tag(tagName);
		em.persist(tag);
		return tag;
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
