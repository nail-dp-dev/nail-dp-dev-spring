package com.backend.naildp.service.post;

import org.springframework.stereotype.Component;

import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.user.FollowRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostAccessValidator {

	private final FollowRepository followRepository;

	public void isAvailablePost(Post post, String username) {
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
	}
}
