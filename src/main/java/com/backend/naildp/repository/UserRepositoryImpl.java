package com.backend.naildp.repository;

import static com.backend.naildp.entity.QArchivePost.*;

import java.util.List;

import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.QArchive;
import com.backend.naildp.entity.QFollow;
import com.backend.naildp.entity.QPost;
import com.backend.naildp.entity.QUser;
import com.backend.naildp.entity.User;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
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

	QFollow follow = QFollow.follow;
	QUser user = QUser.user;
	QPost post = QPost.post;
	QArchive archive = QArchive.archive;

	@Override
	public List<SearchUserResponse> searchByKeyword(String keyword, String nickname) {
		// 사용자 닉네임, 프로필
		// 게시물 수, 저장된 게시물 수, 팔로워 수

		NumberExpression<Integer> userSorting = userSortingOrder(keyword);

		BooleanPath isFollowing = Expressions.booleanPath("isFollowing");

		return queryFactory.select(
				Projections.fields(SearchUserResponse.class, user.nickname, user.thumbnailUrl.as("profileUrl"),
					ExpressionUtils.as(JPAExpressions.select(post.count()).from(post).where(post.user.eq(user)),
						"postCount"), ExpressionUtils.as(JPAExpressions.select(archivePost.count())
						.from(archivePost)
						.where(archivePost.archive.user.eq(user)), "savedPostCount"),
					ExpressionUtils.as(JPAExpressions.select(follow.count()).from(follow).where(follow.following.eq(user)),
						"followerCount"), ExpressionUtils.as(
						JPAExpressions.select(follow.count().when(1L).then(true).otherwise(false))
							.from(follow)
							.where(follow.following.eq(user), follow.follower.nickname.eq(nickname)), isFollowing)))
			.from(user)
			.where(containInNickname(keyword))
			.orderBy(isFollowing.desc(), userSorting.desc(), user.nickname.asc())
			.limit(10)
			.fetch();
	}

	@Override
	public List<SearchUserResponse> findRecommendedUser(User currentUser) {
		QFollow followToOther = new QFollow("followToOther"); // currentUser -> otherUser
		QFollow followToCurrent = new QFollow("followToCurrent"); // otherUser -> currentUser

		return queryFactory
			.select(Projections.fields(SearchUserResponse.class,
				user.nickname.as("nickname"),
				user.thumbnailUrl.as("profileUrl"),
				ExpressionUtils.as(getPostCount(), "postCount"),
				ExpressionUtils.as(getSavedPostCount(), "savedPostCount"),
				ExpressionUtils.as(getFollowerCount(), "followerCount"),
				followToCurrent.id.isNotNull().as("isFollowing")
			))
			.from(user)
			.leftJoin(followToOther)
			.on(followToOther.follower.eq(currentUser)
				.and(followToOther.following.eq(user)))
			.leftJoin(followToCurrent)
			.on(followToCurrent.follower.eq(user)
				.and(followToCurrent.following.eq(currentUser)))
			.where(user.ne(currentUser))
			.groupBy(user.id)
			.orderBy(getPriority(followToOther, followToCurrent).asc(), user.nickname.asc())
			.fetch();
	}

	private NumberExpression<Integer> getPriority(QFollow followCurrentToUser, QFollow followUserToCurrent) {
		return new CaseBuilder()
			// 맞팔일 때 1순위
			.when(followCurrentToUser.id.isNotNull().and(followUserToCurrent.id.isNotNull())).then(1)
			// 내가 팔로우한 경우 2순위
			.when(followCurrentToUser.id.isNotNull()).then(2)
			// 나를 팔로우한 경우 3순위
			.when(followUserToCurrent.id.isNotNull()).then(3)
			.otherwise(4);
	}

	private Expression<Long> getPostCount() {
		return JPAExpressions
			.select(post.count())
			.from(post)
			.where(post.user.eq(user).and(post.tempSave.isFalse()));
	}

	private Expression<Long> getSavedPostCount() {
		return JPAExpressions
			.select(archivePost.count())
			.from(archivePost)
			.join(archivePost.archive, archive)
			.where(archive.user.eq(user));
	}

	private Expression<Long> getFollowerCount() {
		return JPAExpressions
			.select(follow.count())
			.from(follow)
			.where(follow.following.eq(user));
	}

	private NumberExpression<Integer> userSortingOrder(String keyword) {
		return new CaseBuilder().when(user.nickname.startsWith(keyword)).then(2).otherwise(1);
	}

	private BooleanExpression containInNickname(String keyword) {
		return user.nickname.contains(keyword);
	}

}
