package com.backend.naildp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.backend.naildp.dto.notification.NotificationResponseDto;

public interface NotificationCustomRepository {

	Slice<NotificationResponseDto> findNotificationSliceByUsername(Pageable pageable, String username);
}
