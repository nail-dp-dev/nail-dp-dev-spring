package com.backend.naildp.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.CommentLikeRepository;
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
	private final CommentLikeRepository commentLikeRepository;

	@Transactional
	public Long likeComment(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User postWriter = post.getUser();

		//post tempSave 확인
		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		//post boundary 확인
		//  all 이면 모든 사람
		//  follow 면서 follower 나 작성자가 아니면 예외 -> 좋아요 안됨
		//  none 이면 작성자만 가능
		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && (followRepository.existsByFollowerNicknameAndFollowing(username, postWriter)
			|| post.notWrittenBy(username))) {
			throw new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		Comment findComment = post.getComments()
			.stream()
			.filter(comment -> Objects.equals(comment.getId(), commentId))
			.findFirst()
			.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User user = userRepository.findByNickname(username)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		return commentLikeRepository.saveAndFlush(new CommentLike(user, findComment)).getId();
	}
}
