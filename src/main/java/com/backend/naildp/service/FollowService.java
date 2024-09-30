package com.backend.naildp.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowService {

	private final FollowRepository followRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long followUser(String followTargetNickname, String username) {
		// 자기 자신은 팔로워 안됨
		if (followTargetNickname.equals(username)) {
			throw new CustomException("팔로우는 다른 사용자만 가능합니다.", ErrorCode.USER_MISMATCH);
		}

		Optional<Follow> followOptional = followRepository.findFollowByFollowerNicknameAndFollowingNickname(
		username, followTargetNickname);

		if (followOptional.isPresent()) {
			return followOptional.get().getId();
		}

		// 없으면 저장 후 리턴
		User followTargetUser = userRepository.findByNickname(followTargetNickname)
			.orElseThrow(() -> new CustomException("해당 닉네임을 가진 사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User user = userRepository.findByNickname(username)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		return followRepository.saveAndFlush(new Follow(user, followTargetUser)).getId();
	}

	@Transactional
	public void unfollowUser(String followTargetNickname, String username) {
		followRepository.deleteByFollowerNicknameAndFollowingNickname(username, followTargetNickname);
	}

	public int countFollower(String username) {
		return followRepository.countFollowersByUserNickname(username);
	}
}
