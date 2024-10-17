package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
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
	public Slice<NotificationResponseDto> findNotificationSliceByUsername(Pageable pageable, String username) {
		// List<NotificationResponseDto> notificationResponseDtos = queryFactory
		// 	.select(new QNotificationResponseDto(
		// 		notification.sender.nickname,
		// 		notification.sender.thumbnailUrl,
		// 		notification.isRead,
		// 		notification.content,
		// 		notification.createdDate,
		// 		notification.li
		// 	))
		// 	.from(notification)
		// 	.where(notification.receiver.nickname.eq(username)
		// 		.and(notification.createdDate.after(LocalDateTime.now().minusDays(30)))
		// 	)
		// 	.orderBy(notification.isRead.desc(), notification.createdDate.desc())
		// 	.limit(pageable.getPageSize() + 1)
		// 	.fetch();
		//
		// return new SliceImpl<>(notificationResponseDtos, pageable,
		// 	hasNext(notificationResponseDtos, pageable.getPageSize()));
		return null;
	}

	private boolean hasNext(List<NotificationResponseDto> notificationResponseDtos, int size) {
		if (notificationResponseDtos.size() > size) {
			notificationResponseDtos.remove(size);
			return true;
		}
		return false;
	}
}
