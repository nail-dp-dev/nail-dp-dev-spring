package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationCustomRepository {
}
