package com.backend.naildp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.ProfileType;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.userInfo.ProfileRequestDto;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UsersProfile;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.ProfileRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.UsersProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final ProfileRepository profileRepository;
	private final ArchivePostRepository archivePostRepository;
	private final FollowRepository followRepository;
	private final S3Service s3Service;
	private final UsersProfileRepository usersProfileRepository;

	@Transactional(readOnly = true)
	public UserInfoResponseDto getUserInfo(String nickname) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		List<ArchivePost> archivePosts = archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse(
			user.getNickname());

		List<String> followings = followRepository.findFollowingNicknamesByUserNickname(user.getNickname());
		followings.add(nickname);

		int saveCount = calculateSaveCount(archivePosts, followings, user.getNickname());

		return UserInfoResponseDto.builder()
			.nickname(user.getNickname())
			.point(user.getPoint())
			.profileUrl(user.getThumbnailUrl())
			.postsCount(postRepository.countPostsByUserAndTempSaveIsFalse(user))
			.saveCount(saveCount)
			.followerCount(followRepository.countFollowersByUserNickname(user.getNickname()))
			.followingCount(followRepository.countFollowingsByUserNickname(user.getNickname()))
			.build();
	}

	@Transactional(readOnly = true)
	public UserInfoResponseDto getOtherUserInfo(String myNickname, String otherNickname) {

		User otherUser = userRepository.findByNickname(otherNickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		List<ArchivePost> archivePosts = archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse(
			otherNickname);

		List<String> followings = followRepository.findFollowingNicknamesByUserNickname(otherNickname);
		followings.add(otherNickname);

		int saveCount = calculateSaveCount(archivePosts, followings, otherNickname);

		return UserInfoResponseDto.builder()
			.nickname(otherUser.getNickname())
			.point(null)
			.profileUrl(otherUser.getThumbnailUrl())
			.postsCount(postRepository.countPostsByUserAndTempSaveIsFalse(otherUser))
			.saveCount(saveCount)
			.followerCount(followRepository.countFollowersByUserNickname(otherUser.getNickname()))
			.followingCount(followRepository.countFollowingsByUserNickname(otherUser.getNickname()))
			.followingStatus(followRepository.existsByFollowerNicknameAndFollowing(myNickname, otherUser))
			.build();
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getPoint(String nickname) {
		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Map<String, Object> response = new HashMap<>();
		response.put("point", user.getPoint());

		return response;
	}

	@Transactional
	public void uploadProfile(String nickname, MultipartFile file) {
		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		// 업로드하면 바로 해당 사진으로 썸네일 설정
		Profile currentProfile = profileRepository.findProfileByProfileUrl(user.getThumbnailUrl())
			.orElseThrow(() -> new CustomException("현재 프로필 이미지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (checkProfileType(currentProfile)) {
			// CUSTOMIZATION, AUTO 가 아니면 usersProfile delete
			usersProfileRepository.deleteByProfileIdAndUserNickname(currentProfile.getId(), nickname);

		}

		// 프로필 true 포함 4개 이상일 때 업로드 -> 가장 오래된 업로드 이미지 삭제
		int profileNum = usersProfileRepository.countByUserNicknameAndProfileProfileType(nickname,
			ProfileType.CUSTOMIZATION);

		if (profileNum >= 4) {

			UsersProfile oldestUsersProfile = usersProfileRepository.findFirstByUserNicknameAndProfileProfileType(
					nickname, ProfileType.CUSTOMIZATION)
				.orElseThrow(() -> new CustomException("삭제할 프로필 이미지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

			Profile oldestProfile = oldestUsersProfile.getProfile();

			usersProfileRepository.delete(oldestUsersProfile);
			profileRepository.delete(oldestProfile);
			s3Service.deleteFile(oldestProfile.getProfileUrl());
		}

		FileRequestDto fileRequestDto = s3Service.saveFile(file, false);

		Profile newProfile = Profile.builder()
			.profileUrl(fileRequestDto.getFileUrl())
			.name(fileRequestDto.getFileName())
			.profileType(ProfileType.CUSTOMIZATION)
			.build();

		UsersProfile newUsersProfile = UsersProfile.builder()
			.user(user)
			.profile(newProfile)
			.build();

		profileRepository.save(newProfile);
		usersProfileRepository.save(newUsersProfile);
		user.thumbnailUrlUpdate(fileRequestDto.getFileUrl());
	}

	@Transactional(readOnly = true)
	public Map<String, List<String>> getProfiles(String nickname, String choice) {

		ProfileType profileType = ProfileType.valueOf(choice);
		List<String> profileUrls;

		if (profileType == ProfileType.CUSTOMIZATION) {
			profileUrls = usersProfileRepository.findProfileUrlsByNicknameAndCustomization(nickname);
		} else {
			profileUrls = profileRepository.findProfileUrlsByType(profileType);
		}

		Map<String, List<String>> response = new HashMap<>();
		response.put("profileUrls", profileUrls);

		return response;
	}

	@Transactional
	public void changeProfile(String nickname, ProfileRequestDto profileRequestDto) {
		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Profile currentProfile = profileRepository.findProfileByProfileUrl(user.getThumbnailUrl())
			.orElseThrow(() -> new CustomException("현재 프로필 이미지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Profile changingProfile = profileRepository.findProfileByProfileUrl(profileRequestDto.getProfileUrl())
			.orElseThrow(() -> new CustomException("변경할 프로필 이미지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (checkProfileType(currentProfile)) {
			usersProfileRepository.deleteByProfileIdAndUserNickname(currentProfile.getId(), nickname);
		}
		if (checkProfileType(changingProfile)) {
			UsersProfile newUsersProfile = UsersProfile.builder()
				.profile(changingProfile)
				.user(user)
				.build();
			usersProfileRepository.save(newUsersProfile);
		}

		user.thumbnailUrlUpdate(profileRequestDto.getProfileUrl());

	}

	private boolean checkProfileType(Profile profile) {
		return profile.getProfileType() != ProfileType.CUSTOMIZATION && profile.getProfileType() != ProfileType.AUTO;
	}

	private int calculateSaveCount(List<ArchivePost> archivePosts, List<String> followings, String userNickname) {
		if (archivePosts.isEmpty()) {
			return 0;
		}

		return (int)archivePosts.stream()
			.map(ArchivePost::getPost)
			.filter(post -> {
				Boundary boundary = post.getBoundary();
				return boundary == Boundary.ALL ||
					(boundary == Boundary.FOLLOW && followings.contains(post.getUser().getNickname())) ||
					(boundary == Boundary.NONE && !post.notWrittenBy(userNickname));
			})
			.count();
	}
}
