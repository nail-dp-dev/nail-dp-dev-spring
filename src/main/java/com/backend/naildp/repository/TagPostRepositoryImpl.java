package com.backend.naildp.repository;

import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QTag.*;
import static com.backend.naildp.entity.QTagPost.*;
import static com.backend.naildp.entity.QUser.*;

import java.util.List;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.TagPost;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class TagPostRepositoryImpl implements TagPostSearchRepository {

	private final JPAQueryFactory queryFactory;

	public TagPostRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public List<TagPost> searchRelatedTags(String keyword, String userNickname) {
		return queryFactory
			.select(tagPost)
			.from(tagPost)
			.join(tagPost.post, post).fetchJoin().join(tagPost.tag, tag).fetchJoin()
			.where(keywordContainedInTag(keyword)
				.and(post.tempSave.isFalse())
				.and(usernamePermitted(userNickname))
			)
			.orderBy(tag.name.asc())
			.limit(10)
			.fetch();
	}

	private BooleanExpression keywordContainedInTag(String keyword) {
		return tag.name.like(keyword + "%");
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
