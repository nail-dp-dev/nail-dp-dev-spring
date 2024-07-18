package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Profile;
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

		int count = 0;

		if (!archivePosts.isEmpty()) {
			count = (int)archivePosts.stream()
				.map(ArchivePost::getPost)
				.filter(post -> {
					Boundary boundary = post.getBoundary();
					return boundary == Boundary.ALL || (boundary == Boundary.FOLLOW && followings.contains(
						post.getUser().getNickname()));
				})
				.count();
		}

		Profile profile = profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user)
			.orElseThrow(() -> new CustomException("설정된 프로필 썸네일이 없습니다.", ErrorCode.NOT_FOUND));

		return UserInfoResponseDto.builder()
			.nickname(user.getNickname())
			.point(user.getPoint())
			.profileUrl(profile.getProfileUrl())
			.postsCount(postRepository.countPostsByUserAndTempSaveIsFalse(user))
			.saveCount(count)
			.followerCount(followRepository.countFollowersByUserNickname(user.getNickname()))
			.build();
	}
}
