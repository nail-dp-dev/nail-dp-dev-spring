package com.backend.naildp.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostLikeService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final PostLikeRepository postLikeRepository;
	private final FollowRepository followRepository;

	@Transactional
	public Long likeByPostId(Long postId, String username) {

		User user = userRepository.findUserByNickname(username)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 조회할 수 없습니다.", ErrorCode.NOT_FOUND));

		PostLike postLike = postLikeRepository.findPostLikeByUserNicknameAndPostId(username, postId)
			.orElseGet(() -> postLikeRepository.save(new PostLike(user, post)));
		post.addPostLike(postLike);

		return postLike.getId();
	}

	@Transactional
	public void unlikeByPostId(Long postId, String nickname) {
		PostLike postLike = postLikeRepository.findPostLikeByUserNicknameAndPostId(nickname, postId)
			.orElseThrow(() -> new CustomException("해당 게시물은 존재하지 않습니다.", ErrorCode.NOT_FOUND));

		postLikeRepository.deletePostLikeById(postLike.getId());
	}

	public PostLikeCountResponse countPostLike(Long postId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User postWriter = post.getUser();

		//post 에 댓글 달 수 있는지 확인
		//1. tempSave = false 인지
		//2. boundary 가 All 이거나 boundary 가 Follow 면서 username 이 팔로우 하고 있는지까지 확인 필요
		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물은 좋아요를 볼 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 좋아요를 볼 수 없습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username, postWriter)
			&& post.notWrittenBy(username)) {
			throw new CustomException("팔로워 공개 게시물입니다. 팔로워와 작성자만 좋아요할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		return new PostLikeCountResponse(post.getPostLikes().size());
	}
}
