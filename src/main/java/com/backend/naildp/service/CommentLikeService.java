package com.backend.naildp.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentLikeService {

	private final PostRepository postRepository;
	private final FollowRepository followRepository;
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;

	@Transactional
	public Long likeComment(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User postWriter = post.getUser();

		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username, postWriter)
			&& post.notWrittenBy(username)) {
			throw new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		Optional<CommentLike> commentLikeOptional = commentLikeRepository.findCommentLikeByCommentIdAndUserNickname(
			commentId, username);

		if (commentLikeOptional.isPresent()) {
			return commentLikeOptional.get().getId();
		}

		Comment findComment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User user = userRepository.findByNickname(username)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		return commentLikeRepository.saveAndFlush(new CommentLike(user, findComment)).getId();
	}


	@Transactional
	public void cancelCommentLike(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User postWriter = post.getUser();

		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username, postWriter)
			&& post.notWrittenBy(username)) {
			throw new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		commentLikeRepository.findCommentLikeByCommentIdAndUserNickname(commentId, username)
			.ifPresent(commentLikeRepository::delete);
	}

	public PostLikeCountResponse countCommentLikes(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User postWriter = post.getUser();

		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물에는 댓글이 등록할 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username, postWriter)
			&& post.notWrittenBy(username)) {
			throw new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		return new PostLikeCountResponse(commentLikeRepository.countAllByCommentId(commentId));
	}
}
