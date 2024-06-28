package com.backend.naildp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
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

	@Transactional
	public Long likeByPostId(Long postId, String username) {
		User user = userRepository.findUserByNickname(username)
			.orElseThrow(() -> new IllegalArgumentException("nickname 으로 회원을 찾을 수 없습니다."));

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("해당 포스트를 조회할 수 없습니다."));

		PostLike savedPostLike = postLikeRepository.save(new PostLike(user, post));
		return savedPostLike.getId();
	}

	@Transactional
	public void unlikeByPostId(Long postId) {
		postLikeRepository.deletePostLikeByPostId(postId);
	}
}
