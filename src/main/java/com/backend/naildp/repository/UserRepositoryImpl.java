package com.backend.naildp.repository;

import static com.backend.naildp.entity.QArchivePost.*;
import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QUser.*;

import java.util.List;

import com.backend.naildp.dto.search.SearchUserResponse;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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

		NumberExpression<Integer> userSorting = userSortingOrder(keyword);

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
			.where(containInNickname(keyword))
			.orderBy(isFollowing.desc(), userSorting.desc(), user.nickname.asc())
			.limit(10)
			.fetch();
	}

	private NumberExpression<Integer> userSortingOrder(String keyword) {
		return new CaseBuilder()
			.when(user.nickname.startsWith(keyword)).then(2)
			.otherwise(1);
	}

	private BooleanExpression containInNickname(String keyword) {
		return user.nickname.contains(keyword);
	}

}
