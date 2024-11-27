package com.backend.naildp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.CommentLikeRepository;
import com.backend.naildp.repository.CommentRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final PostAccessValidator postAccessValidator;
	private final NotificationManager notificationManager;

	@Transactional
	public Long likeComment(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		postAccessValidator.isAvailablePost(post, username);

		CommentLike savedCommentLike = commentLikeRepository.findCommentLikeByCommentIdAndUserNickname(commentId, username)
			.orElseGet(() -> {
				Comment findComment = commentRepository.findById(commentId)
					.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

				User user = userRepository.findByNickname(username)
					.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

				CommentLike commentLike = commentLikeRepository.saveAndFlush(new CommentLike(user, findComment));

				// notificationManager.handleNotificationFromCommentLike(findComment, user, commentLike);
				notificationManager.handleNotificationFromCommentLikeV2(findComment, user, commentLike);

				return commentLike;
			});

		return savedCommentLike.getId();
	}

	@Transactional
	public void cancelCommentLike(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		postAccessValidator.isAvailablePost(post, username);

		commentLikeRepository.findCommentLikeByCommentIdAndUserNickname(commentId, username)
			.ifPresent(commentLikeRepository::delete);
	}

	@Transactional(readOnly = true)
	public PostLikeCountResponse countCommentLikes(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		postAccessValidator.isAvailablePost(post, username);

		return new PostLikeCountResponse(commentLikeRepository.countAllByCommentId(commentId));
	}

}
