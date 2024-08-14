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
	public Long followUser(String userNickname, String authenticationUsername) {
		// 자기 자신은 팔로워 안됨
		if (userNickname.equals(authenticationUsername)) {
			throw new CustomException("팔로우는 다른 사용자만 가능합니다.", ErrorCode.USER_MISMATCH);
		}

		// 팔로워 관계인지 확인
		Optional<Follow> followOptional = followRepository.findFollowByFollowerNicknameAndFollowingNickname(
			userNickname, authenticationUsername);

		// 맞다면 그냥 리턴
		if (followOptional.isPresent()) {
			return followOptional.get().getId();
		}

		// 없으면 저장 후 리턴
		User followingUser = userRepository.findByNickname(userNickname)
			.orElseThrow(() -> new CustomException("해당 닉네임을 가진 사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		User followerUser = userRepository.findByNickname(authenticationUsername)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		return followRepository.saveAndFlush(new Follow(followerUser, followingUser)).getId();
	}

	@Transactional
	public void unfollowUser(String userNickname, String authenticationUsername) {
		followRepository.deleteByFollowerNicknameAndFollowingNickname(authenticationUsername, userNickname);
	}
}
