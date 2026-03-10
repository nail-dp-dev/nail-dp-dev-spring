package com.backend.naildp.service.comment;

import com.backend.naildp.service.notification.NotificationManager;
import com.backend.naildp.service.post.PostAccessValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.entity.commentEntity.Comment;
import com.backend.naildp.entity.commentEntity.CommentLike;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.comment.CommentLikeRepository;
import com.backend.naildp.repository.comment.CommentRepository;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
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

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User user = userRepository.findByNickname(username)
				.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
			log.info("이미 존재하는 엔티티입니다.");
			return 0L;
		}

		CommentLike commentLike = new CommentLike(user, comment);
		CommentLike savedCommentLike = commentLikeRepository.saveAndFlush(commentLike);

		notificationManager.handleNotificationFromCommentLike(comment, user, savedCommentLike);

		return savedCommentLike.getId();
	}

	@Transactional
	public void likeCommentConcurrently(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
				.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		postAccessValidator.isAvailablePost(post, username);

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User user = userRepository.findByNickname(username)
				.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
			log.info("이미 존재하는 엔티티입니다.");
			return;
		}

		CommentLike commentLike = new CommentLike(user, comment);

		try {
			CommentLike savedCommentLike = commentLikeRepository.saveAndFlush(commentLike);
			notificationManager.handleNotificationFromCommentLike(comment, user, savedCommentLike);
		} catch (DataIntegrityViolationException e) {
			log.warn("데이터 무결성 위배 : {}", e.getMessage());
			log.info("이미 존재하는 데이터이므로 무시하고 넘어간다.");
		} catch (Exception e) {
			log.error("error: {}", e.getMessage(), e);
			throw e;
		}
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
