package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationCustomRepository {

	List<Notification> findNotificationsByIdInAndReceiverNickname(List<Long> ids, String nickname);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Notification n set n.isRead = true where n in :notifications")
	int changeReadStatus(@Param("notifications") List<Notification> notifications);


}
