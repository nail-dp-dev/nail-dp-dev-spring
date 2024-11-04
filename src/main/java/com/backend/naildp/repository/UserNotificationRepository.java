package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.entity.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	@Query("select un from UserNotification un where un.user.nickname = :nickname")
	List<UserNotification> findNotificationTypeByUserNickname(@Param("nickname") String nickname);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update UserNotification un set un.isEnabled = :status"
		+ " where un.user.nickname = :nickname and un.notificationType = :type")
	int updateUserNotificationByNotificationType(@Param("status") boolean status, @Param("nickname") String nickname,
		@Param("type") NotificationType notificationType);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update UserNotification un set un.isEnabled = :status where un.user.nickname = :nickname")
	int updateAllNotificationType(@Param("status") boolean status, @Param("nickname") String nickname);
}
