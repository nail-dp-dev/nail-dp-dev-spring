package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
}
