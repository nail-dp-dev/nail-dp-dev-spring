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
			.link("/user/" + followerUser.getNickname())
			.build();
	}

	public static Notification fromPostLike(PostLike postLike) {
		Post likedPost = postLike.getPost();
		User receiver = likedPost.getUser();
		User sender = postLike.getUser();

		return Notification.builder()
			.receiver(receiver)
			.sender(sender)
			.content(sender.getNickname() + "가 회원님의 게시물을 좋아합니다.")
			.notificationType(NotificationType.POST_LIKE)
			.isRead(false)
			.link("/posts/" + likedPost.getId().toString())
			.build();
	}

	public static Notification fromCommentLike(CommentLike commentLike) {
		Comment comment = commentLike.getComment();
		User commentOwner = comment.getUser();
		User sender = commentLike.getUser();

		return Notification.builder()
			.receiver(commentOwner)
			.sender(sender)
			.content(sender.getNickname() + "님이 회원님의 댓글을 좋아합니다.")
			.notificationType(NotificationType.COMMENT_LIKE)
			.isRead(false)
			.link("/posts/" + comment.getPost().getId().toString())
			.build();
	}

	public static Notification fromComment(Comment comment) {
		User postOwner = comment.getPost().getUser();
		User sender = comment.getUser();

		return Notification.builder()
			.receiver(postOwner)
			.sender(sender)
			.content(sender.getNickname() + "님이 회원님의 게시물에 댓글을 등록했습니다. " + comment.getCommentContent())
			.notificationType(NotificationType.COMMENT)
			.isRead(false)
			.link("/posts/" + comment.getPost().getId().toString())
			.build();
	}

	public void readNotification() {
		this.isRead = true;
	}
}
