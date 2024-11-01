package com.backend.naildp.entity;

import com.backend.naildp.common.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotification {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_notification_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	private NotificationType notificationType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public UserNotification(NotificationType notificationType, User user) {
		this.notificationType = notificationType;
		this.user = user;
	}
}
