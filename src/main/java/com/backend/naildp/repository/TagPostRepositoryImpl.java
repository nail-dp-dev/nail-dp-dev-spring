package com.backend.naildp.repository;

import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QTag.*;
import static com.backend.naildp.entity.QTagPost.*;
import static com.backend.naildp.entity.QUser.*;

import java.util.List;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.TagPost;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class TagPostRepositoryImpl implements TagPostSearchRepository {

	private final JPAQueryFactory queryFactory;

	public TagPostRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public List<TagPost> searchRelatedTags(List<String> keywords, String userNickname) {
		BooleanBuilder builder = new BooleanBuilder();
		for (String keyword : keywords) {
			builder.or(tag.name.startsWithIgnoreCase(keyword));
		}

		CaseBuilder caseBuilder = new CaseBuilder();
		NumberExpression<Integer> startWithAnyKeyword = caseBuilder.when(builder).then(1)
			.otherwise(0);

		return queryFactory
			.select(tagPost)
			.from(tagPost)
			.join(tagPost.post, post).fetchJoin().join(tagPost.tag, tag).fetchJoin()
			.where(post.tempSave.isFalse()
				.and(usernamePermitted(userNickname))
				.and(keywordContainedInTag(keywords))
			)
			.orderBy(tag.name.length().asc(), startWithAnyKeyword.desc(), tag.name.asc())
			.fetch();
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
}
