package com.backend.naildp.repository;

import static com.backend.naildp.entity.QNotification.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.backend.naildp.dto.notification.NotificationResponseDto;
import com.backend.naildp.dto.notification.QNotificationResponseDto;
import com.backend.naildp.entity.QNotification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class NotificationRepositoryImpl implements NotificationCustomRepository {

	private final JPAQueryFactory queryFactory;

	public NotificationRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Slice<NotificationResponseDto> findNotificationSliceByUsername(Pageable pageable, String username,
		Long cursorNotificationId) {

		List<NotificationResponseDto> notificationResponseDtos = queryFactory
			.select(new QNotificationResponseDto(
				notification.sender.nickname,
				notification.sender.thumbnailUrl,
				notification.isRead,
				notification.content,
				notification.createdDate
			))
			.from(notification)
			.where(notification.receiver.nickname.eq(username)
				.and(notification.createdDate.after(LocalDateTime.now().minusDays(30)))
				.and(isBeforeNotification(cursorNotificationId))
			)
			.orderBy(notification.isRead.desc(), notification.createdDate.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return new SliceImpl<>(notificationResponseDtos, pageable,
			hasNext(notificationResponseDtos, pageable.getPageSize()));
	}

	private BooleanExpression isBeforeNotification(Long cursorNotificationId) {
		if (cursorNotificationId == null) {
			return null;
		}
		QNotification cursorNotification = new QNotification("cursorNotification");
		return JPAExpressions
			.select(cursorNotification.createdDate)
			.from(cursorNotification)
			.where(cursorNotification.id.eq(cursorNotificationId)).lt(notification.createdDate);
	}

	private boolean hasNext(List<NotificationResponseDto> notificationResponseDtos, int size) {
		if (notificationResponseDtos.size() > size) {
			notificationResponseDtos.remove(size);
			return true;
		}
		return false;
	}
}
