package com.backend.naildp.repository;

import org.springframework.data.domain.Slice;

import com.backend.naildp.dto.notification.NotificationResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class NotificationRepositoryImpl implements NotificationCustomRepository {

	private final JPAQueryFactory queryFactory;

	public NotificationRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Slice<NotificationResponseDto> findNotificationSliceByUsername(String username) {

		return null;
	}
}
