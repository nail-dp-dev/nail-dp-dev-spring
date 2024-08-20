package com.backend.naildp.repository;

import static com.backend.naildp.entity.QArchivePost.*;
import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QUser.*;

import java.util.List;

import com.backend.naildp.dto.search.QSearchUserResponse;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.QArchivePost;
import com.backend.naildp.entity.QFollow;
import com.backend.naildp.entity.QPost;
import com.backend.naildp.entity.QUser;
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
		return queryFactory
			.select(new QSearchUserResponse(
				user.nickname,
				user.thumbnailUrl,
				JPAExpressions
					.select(post.count())
					.from(post)
					.where(post.user.eq(user)),
				JPAExpressions
					.select(archivePost.count())
					.from(archivePost)
					.where(archivePost.archive.user.eq(user)),
				follow.count(),
				follow.isNotNull()
			))
			.from(user)
			.leftJoin(follow).on(user.eq(follow.following))
			.orderBy(follow.isNotNull().asc(), user.nickname.asc())
			.limit(10)
			.fetch();
	}
}
