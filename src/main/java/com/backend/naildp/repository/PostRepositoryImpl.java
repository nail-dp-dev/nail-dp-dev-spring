package com.backend.naildp.repository;

import static com.backend.naildp.entity.QArchivePost.*;
import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QPostLike.*;
import static com.backend.naildp.entity.QTagPost.*;
import static com.backend.naildp.entity.QUser.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringExpressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.jsonwebtoken.lang.Strings;
import jakarta.persistence.EntityManager;

public class PostRepositoryImpl implements PostSearchRepository {

	private final JPAQueryFactory queryFactory;

	public PostRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Slice<Post> searchPostByKeyword(Pageable pageable, List<String> keywords, String username, Long cursorId) {
		List<Post> posts = queryFactory
			.select(post)
			.from(post)
			.where(
				isAllowedToViewPosts(username)
					.and(containsInPost(keywords))
					.and(lessThanCustomCursor(cursorId))
			)
			.orderBy(post.postLikes.size().desc(), post.createdDate.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return new SliceImpl<>(posts, pageable, hasNext(posts, pageable.getPageSize()));
	}

	@Override
	public List<Post> findPostsInArchive(String username) {
		return queryFactory
			.select(archivePost.post)
			.from(archivePost)
			.where(archivePost.archive.user.nickname.eq(username))
			.fetch();
	}

	@Override
	public List<Post> findLikedPosts(String username) {
		return queryFactory
			.select(postLike.post)
			.from(postLike)
			.where(postLike.user.nickname.eq(username))
			.fetch();
	}

	private boolean hasNext(List<Post> posts, int size) {
		if (posts.size() > size) {
			posts.remove(size);
			return true;
		}
		return false;
	}

	private BooleanExpression isAllowedToViewPosts(String usernameCond) {
		if (!Strings.hasText(usernameCond)) {
			return null;
		}

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

	private BooleanBuilder containsInPost(List<String> keywords) {
		if (keywords.isEmpty()) {
			return null;
		}

		BooleanBuilder builder = new BooleanBuilder();

		keywords.forEach(keyword -> {
			if (StringUtils.hasText(keyword)) {
				builder.and(postContentContains(keyword).or(postTagsContains(keyword)));
			}
		});

		return builder;
	}

	private BooleanExpression postContentContains(String keyword) {

		return post.postContent.contains(keyword);
	}

	private BooleanExpression postTagsContains(String keyword) {

		return JPAExpressions
			.select(tagPost.tag.name)
			.from(tagPost)
			.where(tagPost.post.eq(post))
			.contains(keyword);
	}

	private BooleanExpression lessThanCustomCursor(Long cursorId) {
		if (cursorId == null) {
			return null;
		}

		DateTimeTemplate<String> stringDateTimeTemplate = Expressions.dateTimeTemplate(String.class,
			"DATE_FORMAT({0}, '%y%m%d%H%i%s')", post.createdDate);

		String cursor = generateCustomCursor(cursorId, stringDateTimeTemplate);

		return generateCustomExpression(stringDateTimeTemplate).lt(cursor);
	}

	private String generateCustomCursor(Long cursorId, DateTimeTemplate<String> stringDateTimeTemplate) {
		return queryFactory
			.select(generateCustomExpression(stringDateTimeTemplate))
			.from(post)
			.where(post.id.eq(cursorId))
			.fetchOne();
	}

	private StringExpression generateCustomExpression(DateTimeTemplate<String> stringDateTimeTemplate) {
		return StringExpressions.lpad(post.postLikes.size().stringValue(), 6, '0')
			.concat(StringExpressions.lpad(stringDateTimeTemplate.stringValue(), 12, '0')
				.concat(StringExpressions.lpad(post.id.stringValue(), 8, '0')));
	}

}
