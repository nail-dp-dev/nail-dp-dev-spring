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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	private String content;

	@Enumerated(value = EnumType.STRING)
	private NotificationType notificationType;

	private boolean isRead;

	private String link;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id")
	private User receiver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id")
	private User sender;

	@Builder
	public Notification(String content, NotificationType notificationType, boolean isRead, String link, User receiver, User sender) {
		this.content = content;
		this.notificationType = notificationType;
		this.isRead = isRead;
		this.link = link;
		this.receiver = receiver;
		this.sender = sender;
	}

	public static Notification followOf(Follow follow) {
		User followerUser = follow.getFollower();
		User followingUser = follow.getFollowing();

		return Notification.builder()
			.receiver(followingUser)
			.sender(followerUser)
			.content(followerUser.getNickname() + "님이 회원님을 팔로우했습니다.")
			.notificationType(NotificationType.FOLLOW)
			.isRead(false)
			.link("/api/user/" + followerUser.getNickname())
			.build();
	}
}
