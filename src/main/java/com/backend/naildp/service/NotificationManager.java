package com.backend.naildp.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationManager {

	private final NotificationService notificationService;
	private final ApplicationEventPublisher applicationEventPublisher;

	public void handleNotificationFromCommentLike(Comment comment, User user, CommentLike commentLike) {
		if (comment.notRegisteredBy(user)) {
			Notification notificationByCommentLike = Notification.fromCommentLike(commentLike);
			Notification savedNotification = notificationService.save(notificationByCommentLike);

			User receiver = savedNotification.getReceiver();
			if (receiver.allowsNotificationType(savedNotification.getNotificationType())) {
				applicationEventPublisher.publishEvent(savedNotification);
			}
		}
	}

	public void handleCommentNotification(Comment comment, User postWriter) {
		if (comment.notRegisteredBy(postWriter)) {
			Notification savedNotification = notificationService.save(Notification.fromComment(comment));

			User receiver = savedNotification.getReceiver();
			if (receiver.allowsNotificationType(savedNotification.getNotificationType())) {
				applicationEventPublisher.publishEvent(savedNotification);
			}
		}
	}

	public void handlePostLikeNotification(User user, Post post, PostLike postLike) {
		if (post.notWrittenBy(user)) {
			Notification savedNotification = notificationService.save(Notification.fromPostLike(postLike));

			User receiver = savedNotification.getReceiver();
			if (receiver.allowsNotificationType(savedNotification.getNotificationType())) {
				applicationEventPublisher.publishEvent(savedNotification);
			}
		}
	}
}
