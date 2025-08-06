package com.backend.naildp.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.service.dto.PostLikeEvent;
import com.backend.naildp.service.dto.PostUnlikeEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostLikeService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final PostLikeRepository postLikeRepository;
	private final PostAccessValidator postAccessValidator;
	private final NotificationManager notificationManager;
	private final ApplicationEventPublisher postLikeEventPublisher;

	@Transactional
	public Long likeByPostId(Long postId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 조회할 수 없습니다.", ErrorCode.NOT_FOUND));
		postAccessValidator.isAvailablePost(post, username);

		PostLike postLike = postLikeRepository.findPostLikeByUserNicknameAndPostId(username, postId)
			.orElseGet(() -> {
				User user = userRepository.findUserByNickname(username)
					.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
				PostLike savedPostLike = postLikeRepository.save(new PostLike(user, post));
				post.addPostLike(savedPostLike);

				postLikeEventPublisher.publishEvent(new PostLikeEvent(post, user));

				notificationManager.handlePostLikeNotification(user, post, savedPostLike);

				return savedPostLike;
			});

		return postLike.getId();
	}

	@Transactional
	public void unlikeByPostId(Long postId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		postAccessValidator.isAvailablePost(post, username);

		PostLike postLike = postLikeRepository.findPostLikeByUserNicknameAndPostId(username, postId)
			.orElseThrow(() -> new CustomException("해당 게시물은 존재하지 않습니다.", ErrorCode.NOT_FOUND));

		postLikeEventPublisher.publishEvent(new PostUnlikeEvent(post, postLike.getUser()));

		postLikeRepository.deletePostLikeById(postLike.getId());
	}

	@Transactional(readOnly = true)
	public PostLikeCountResponse countPostLike(Long postId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		postAccessValidator.isAvailablePost(post, username);

		return new PostLikeCountResponse(post.getPostLikes().size());
	}
}
