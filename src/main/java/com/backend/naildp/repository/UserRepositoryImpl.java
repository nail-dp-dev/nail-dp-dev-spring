package com.backend.naildp.repository;

import static com.backend.naildp.entity.QArchivePost.*;
import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QUser.*;

import java.util.List;

import com.backend.naildp.dto.search.SearchUserResponse;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class UserRepositoryImpl implements UserRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public UserRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public List<SearchUserResponse> searchByKeyword(String keyword, String nickname) {
		// 사용자 닉네임, 프로필
		// 게시물 수, 저장된 게시물 수, 팔로워 수
		BooleanPath isFollowing = Expressions.booleanPath("isFollowing");

		return queryFactory
			.select(Projections.fields(SearchUserResponse.class,
				user.nickname,
				user.thumbnailUrl.as("profileUrl"),
				ExpressionUtils.as(
					JPAExpressions
						.select(post.count())
						.from(post)
						.where(post.user.eq(user)), "postCount"),
				ExpressionUtils.as(
					JPAExpressions
						.select(archivePost.count())
						.from(archivePost)
						.where(archivePost.archive.user.eq(user)), "savedPostCount"),
				ExpressionUtils.as(
					JPAExpressions
						.select(follow.count())
						.from(follow)
						.where(follow.following.eq(user)), "followerCount"),
				ExpressionUtils.as(
					JPAExpressions
						.select(follow.count().when(1L).then(true)
							.otherwise(false))
						.from(follow)
						.where(follow.following.eq(user), follow.follower.nickname.eq(nickname)), isFollowing)
			))
			.from(user)
			.where(user.nickname.like(keyword + "%"))
			.orderBy(isFollowing.desc(), user.nickname.asc())
			.limit(10)
			.fetch();
		// return null;
	}

}
