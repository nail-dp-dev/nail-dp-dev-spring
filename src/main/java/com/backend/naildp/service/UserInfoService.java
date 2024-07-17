package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.ProfileRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInfoService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final ProfileRepository profileRepository;
	private final ArchivePostRepository archivePostRepository;
	private final FollowRepository followRepository;

	public UserInfoResponseDto getUserInfo(String nickname) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		List<ArchivePost> archivePosts = archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse(
			user.getNickname());

		List<String> followings = followRepository.findFollowingNicknamesByUserNickname(user.getNickname());

		int count = (int)archivePosts.stream()
			.filter(archivePost -> archivePost.getPost().getBoundary() == Boundary.ALL || (
				archivePost.getPost().getBoundary() == Boundary.FOLLOW && followings.contains(
					archivePost.getPost().getUser().getNickname())))
			.count();

		return UserInfoResponseDto.builder()
			.nickname(user.getNickname())
			.point(user.getPoint())
			.profileUrl(profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user).getProfileUrl())
			.postsCount(postRepository.countPostsByUserAndTempSaveIsFalse(user))
			.saveCount(count)
			.followerCount(followRepository.countFollowersByUserNickname(user.getNickname()))
			.build();
	}
}
