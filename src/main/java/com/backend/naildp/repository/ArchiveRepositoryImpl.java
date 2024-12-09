package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.archive.FollowArchiveResponseDto;
import com.backend.naildp.dto.archive.UserArchiveResponseDto;
import com.backend.naildp.entity.QArchive;
import com.backend.naildp.entity.QArchivePost;
import com.backend.naildp.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class ArchiveRepositoryImpl implements ArchiveCustomRepository {

	private final JPAQueryFactory queryFactory;

	public ArchiveRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Slice<UserArchiveResponseDto> findUserArchives(String nickname, Long cursorId, int size) {
		QArchivePost archivePost = QArchivePost.archivePost;

		List<UserArchiveResponseDto> results = queryFactory
			.select(Projections.constructor(
				UserArchiveResponseDto.class,
				QArchive.archive.id.as("archiveId"),
				QArchive.archive.name.as("archiveName"),
				QArchive.archive.boundary,
				QArchive.archive.archiveImgUrl,
				archivePost.countDistinct().as("postCount"),
				Expressions.asBoolean(true).as("isLock")
			))
			.from(QArchive.archive)
			.leftJoin(archivePost).on(archivePost.archive.eq(QArchive.archive))
			.where(
				QArchive.archive.user.nickname.eq(nickname),
				cursorId != -1 ? QArchive.archive.id.lt(cursorId) : null

			)
			.groupBy(QArchive.archive.id)
			.orderBy(QArchive.archive.createdDate.desc())
			.limit(size + 1)
			.fetch();

		return createSlice(results, size);

	}

	@Override
	public Slice<UserArchiveResponseDto> findOtherUserArchives(String otherNickname, Long cursorId, int size) {
		QArchivePost archivePost = QArchivePost.archivePost;

		List<UserArchiveResponseDto> results = queryFactory
			.select(Projections.constructor(
				UserArchiveResponseDto.class,
				QArchive.archive.id.as("archiveId"),
				QArchive.archive.name.as("archiveName"),
				QArchive.archive.boundary,
				QArchive.archive.archiveImgUrl,
				archivePost.countDistinct().as("postCount"),
				Expressions.asBoolean(true).as("isLock")
			))
			.from(QArchive.archive)
			.leftJoin(archivePost).on(archivePost.archive.eq(QArchive.archive))
			.where(
				QArchive.archive.user.nickname.eq(otherNickname),
				QArchive.archive.boundary.ne(Boundary.NONE),
				cursorId != -1 ? QArchive.archive.id.lt(cursorId) : null
			)
			.groupBy(QArchive.archive.id)
			.orderBy(QArchive.archive.createdDate.desc())
			.limit(size + 1)
			.fetch();

		return createSlice(results, size);
	}

	@Override
	public Slice<FollowArchiveResponseDto> findFollowingArchives(List<String> followingNicknames, Long cursorId,
		int size) {
		QArchive archive = QArchive.archive;
		QUser user = QUser.user;

		List<FollowArchiveResponseDto> results = queryFactory
			.select(Projections.constructor(
				FollowArchiveResponseDto.class,
				archive.id.as("archiveId"),
				user.archiveCount.as("archiveCount"),
				user.nickname.as("nickname"),
				archive.archiveImgUrl.as("archiveImgUrl"),
				user.thumbnailUrl.as("profileUrl")
			))
			.from(archive)
			.join(archive.user, user)
			.where(
				user.nickname.in(followingNicknames),
				cursorId != -1 ? archive.id.lt(cursorId) : null,
				archive.createdDate.eq(
					JPAExpressions.select(archive.createdDate.max())
						.from(archive)
						.where(
							archive.user.eq(user),
							archive.boundary.ne(Boundary.NONE)
						)
				)
			)
			.orderBy(archive.createdDate.desc())
			.limit(size + 1)
			.fetch();

		return createSlice(results, size);
	}

	@Override
	public Slice<UserArchiveResponseDto> findSavedArchives(String nickname, Long postId, Long cursorId, int size) {
		QArchive archive = QArchive.archive;
		QArchivePost archivePost = QArchivePost.archivePost;

		List<UserArchiveResponseDto> results = queryFactory
			.select(Projections.constructor(
				UserArchiveResponseDto.class,
				archive.id.as("archiveId"),
				archive.name.as("archiveName"),
				archive.boundary,
				archive.archiveImgUrl.as("archiveImgUrl"),
				archivePost.countDistinct().as("postCount"),
				Expressions.constant(true)
			))
			.from(archive)
			.leftJoin(archivePost).on(archivePost.archive.eq(archive))
			.where(
				archive.user.nickname.eq(nickname),
				archivePost.post.id.eq(postId),
				cursorId != -1 ? archive.id.lt(cursorId) : null
			)
			.groupBy(archive.id)
			.orderBy(archive.createdDate.desc())
			.limit(size + 1)
			.fetch();

		return createSlice(results, size);
	}

	private <T> Slice<T> createSlice(List<T> content, int size) {
		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}
		return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
	}

}
