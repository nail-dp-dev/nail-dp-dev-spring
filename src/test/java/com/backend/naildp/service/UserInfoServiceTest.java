package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.ProfileType;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.userInfo.ProfileRequestDto;
import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UsersProfile;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.ProfileRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.UsersProfileRepository;

class UserInfoServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private ArchivePostRepository archivePostRepository;
	@Mock
	private FollowRepository followRepository;
	@InjectMocks
	private UserInfoService userInfoService;
	@Mock
	private UsersProfileRepository usersProfileRepository;
	@Mock
	S3Service s3Service;

	private User user1;
	private User user2;
	private Profile profile1;
	private Post post1;
	private Post post2;
	private Post post3;
	private Archive archive1;
	private ArchivePost archivePost1;
	private ArchivePost archivePost2;
	private Follow follow1;

	private int followerCount;

	private UsersProfile usersProfile1;
	private UsersProfile userProfile2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user1 = new User("alswl", "010-1234-5678", 1000L, UserRole.USER);
		user1 = User.builder()
			.nickname("alswl")
			.agreement(true)
			.thumbnailUrl("alswl.profileUrl.jpg")
			.point(1000L)
			.phoneNumber("010-1234-5678")
			.role(UserRole.USER)
			.build();
		profile1 = Profile.builder()
			.profileType(ProfileType.CUSTOMIZATION)
			.name("name")
			.profileUrl("alswl.profileUrl.jpg")
			.thumbnail(true)
			.build();

		usersProfile1 = UsersProfile.builder()
			.user(user1)
			.profile(profile1)
			.build();

		post1 = new Post(user1, "alswl postContent", 0L, Boundary.ALL, false);

		user2 = new User("jw", "010-9876-5432", 0L, UserRole.USER);
		post2 = new Post(user2, "jw postContent", 0L, Boundary.ALL, false);
		post3 = new Post(user2, "jw postContent", 0L, Boundary.ALL, true);

		archive1 = new Archive(user1, "alswl archive", Boundary.ALL);
	}

	@DisplayName("사용자 정보 조회 - 저장한 포스트의 공개범위가 전체일 떄")
	@Test
	void testGetUserInfo_PostBoundaryAll() {
		// Given
		archivePost1 = new ArchivePost(archive1, post2);
		followerCount = 0;

		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(Collections.emptyList());
		given(usersProfileRepository.findProfileUrlByNicknameAndThumbnailTrue(user1.getNickname())).willReturn(
			Optional.of(profile1.getProfileUrl()));
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getNickname()).isEqualTo("alswl");
		assertThat(userInfoResponseDto.getPoint()).isEqualTo(1000L);
		assertThat(userInfoResponseDto.getProfileUrl()).isEqualTo("alswl.profileUrl.jpg");
		assertThat(userInfoResponseDto.getPostsCount()).isEqualTo(1);
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(1);
		assertThat(userInfoResponseDto.getFollowerCount()).isEqualTo(followerCount);
	}

	@DisplayName("사용자 정보 조회 - 저장한 포스트의 공개범위가 팔로워일 때_notFollowing")
	@Test
	void testGetUserInfo_PostBoundaryFollow1() {
		// Given
		post2 = new Post(user2, "jw postContent", 0L, Boundary.FOLLOW, false);
		archivePost1 = new ArchivePost(archive1, post2);

		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(Collections.emptyList());
		given(usersProfileRepository.findProfileUrlByNicknameAndThumbnailTrue(user1.getNickname())).willReturn(
			Optional.of(profile1.getProfileUrl()));
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(0);
	}

	@DisplayName("사용자 정보 조회 - 저장한 포스트의 공개범위가 팔로워일 때_Following")
	@Test
	void testGetUserInfo_PostBoundaryFollow2() {
		// Given
		post2 = new Post(user2, "jw postContent", 0L, Boundary.FOLLOW, false);
		Post post4 = new Post(user2, "jw postContent", 0L, Boundary.FOLLOW, false);
		archivePost1 = new ArchivePost(archive1, post2);
		archivePost2 = new ArchivePost(archive1, post4);

		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1, archivePost2));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(
			List.of(user2.getNickname(), "user3"));
		given(usersProfileRepository.findProfileUrlByNicknameAndThumbnailTrue(user1.getNickname())).willReturn(
			Optional.of(profile1.getProfileUrl()));
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(2);
	}

	@DisplayName("사용자 정보 조회 - 저장한 포스트의 공개범위가 비공개일 때")
	@Test
	void testGetUserInfo_PostBoundaryNone() {
		// Given
		post2 = new Post(user2, "jw postContent", 0L, Boundary.NONE, false);
		archivePost1 = new ArchivePost(archive1, post2);
		followerCount = 1;
		given(userRepository.findByNickname("alswl")).willReturn(Optional.of(user1));
		given(archivePostRepository.findAllArchivePostsByUserNicknameAndTempSaveIsFalse("alswl")).willReturn(
			List.of(archivePost1));
		given(followRepository.findFollowingNicknamesByUserNickname("alswl")).willReturn(List.of(user2.getNickname()));
		given(usersProfileRepository.findProfileUrlByNicknameAndThumbnailTrue(user1.getNickname())).willReturn(
			Optional.of(profile1.getProfileUrl()));
		given(postRepository.countPostsByUserAndTempSaveIsFalse(user1)).willReturn(1);
		given(followRepository.countFollowersByUserNickname("alswl")).willReturn(followerCount);

		// When
		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo("alswl");

		// Then
		assertThat(userInfoResponseDto.getSaveCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("사용자 정보 조회 - 사용자를 찾지 못할 때 예외 발생")
	void testGetUserInfo_UserNotFound() {
		// Given
		given(userRepository.findByNickname("alswl")).willReturn(Optional.empty());

		// Then
		assertThrows(CustomException.class, () -> userInfoService.getUserInfo("user1"));
	}

	@Test
	@DisplayName("포인트 조회")
	void testGetPoint() {
		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));

		Map<String, Object> result = userInfoService.getPoint("alswl");

		assertThat(result.get("point")).isEqualTo(1000L);
		verify(userRepository, times(1)).findByNickname(anyString());
	}

	@Test
	@DisplayName("포인트 조회 - nickname없음")
	void testGetPoint_UserNotFound() {
		given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

		CustomException exception = assertThrows(CustomException.class, () -> {
			userInfoService.getPoint("testNickname");
		});

		assertThat(exception.getMessage()).isEqualTo("nickname 으로 회원을 찾을 수 없습니다.");
		verify(userRepository, times(1)).findByNickname(anyString());
	}

	@Test
	@DisplayName("프로필 업로드 성공 - 4개 이상")
	void testUploadProfile1() {
		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));
		given(usersProfileRepository.findByUserNicknameAndProfileThumbnailTrue(anyString())).willReturn(
			Optional.of(usersProfile1));
		given(usersProfileRepository.countByUserNicknameAndProfileProfileType(anyString(),
			any())).willReturn(4);
		given(usersProfileRepository.findFirstByUserNicknameAndProfileProfileType(anyString(),
			any())).willReturn(
			Optional.of(usersProfile1));
		given(s3Service.saveFile(any(MultipartFile.class))).willReturn(new FileRequestDto("file1", 12345L, "fileUrl1"));

		userInfoService.uploadProfile("nickname", mock(MultipartFile.class));

		verify(usersProfileRepository, times(1)).delete(any(UsersProfile.class));
		verify(profileRepository, times(1)).delete(any(Profile.class));
		verify(s3Service, times(1)).deleteFile(anyString());
		verify(profileRepository, times(1)).save(any(Profile.class));
		verify(usersProfileRepository, times(1)).save(any(UsersProfile.class));
	}

	@Test
	@DisplayName("프로필 업로드 성공 - 3개 이하")
	void testUploadProfile2() {
		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));
		given(usersProfileRepository.findByUserNicknameAndProfileThumbnailTrue(anyString())).willReturn(
			Optional.of(usersProfile1));
		given(usersProfileRepository.countByUserNicknameAndProfileProfileType(anyString(),
			any())).willReturn(3);
		given(s3Service.saveFile(any(MultipartFile.class))).willReturn(new FileRequestDto("file1", 12345L, "fileUrl1"));

		userInfoService.uploadProfile("testNickname", mock(MultipartFile.class));

		verify(profileRepository, times(1)).save(any(Profile.class));
		verify(usersProfileRepository, times(1)).save(any(UsersProfile.class));
	}

	@Test
	@DisplayName("프로필 업로드 실패 - 현재 프로필")
	void testUploadProfile_ProfileNotFound() {
		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));
		given(usersProfileRepository.findByUserNicknameAndProfileThumbnailTrue(anyString())).willReturn(
			Optional.empty());

		CustomException exception = assertThrows(CustomException.class, () -> {
			userInfoService.uploadProfile("testNickname", mock(MultipartFile.class));
		});

		assertThat(exception.getMessage()).isEqualTo("현재 프로필 이미지를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("프로필 리스트 조회 성공 - CUSTOMIZATION")
	void testGetProfiles_Customization() {
		List<String> mockProfileUrls = Arrays.asList("test.jpg", "test.jpg");

		given(usersProfileRepository.findProfileUrlsByNicknameAndCustomization(anyString())).willReturn(
			mockProfileUrls);

		Map<String, List<String>> result = userInfoService.getProfiles("testNickname", "CUSTOMIZATION");

		assertThat(result.get("profileUrls")).isEqualTo(mockProfileUrls);

		verify(usersProfileRepository, times(1)).findProfileUrlsByNicknameAndCustomization(anyString());
		verify(profileRepository, times(0)).findProfileUrlsByType(any(ProfileType.class));
	}

	@Test
	@DisplayName("프로필 리스트 조회 성공 - BASIC,ICON")
	void testGetProfiles_OtherType() {
		List<String> mockProfileUrls = Arrays.asList("test.jpg", "test.jpg");

		given(profileRepository.findProfileUrlsByType(any(ProfileType.class))).willReturn(mockProfileUrls);

		Map<String, List<String>> result = userInfoService.getProfiles("testNickname", "BASIC");

		assertThat(result.get("profileUrls")).isEqualTo(mockProfileUrls);

		verify(usersProfileRepository, times(0)).findProfileUrlsByNicknameAndCustomization(anyString());
		verify(profileRepository, times(1)).findProfileUrlsByType(any(ProfileType.class));
	}

	@Test
	@DisplayName("프로필 변경 성공 - CUSTOM -> CUSTOM")
	void testChangeProfile1() {
		ProfileRequestDto profileRequestDto = ProfileRequestDto.builder()
			.profileUrl("changingProfileURl")
			.build();

		Profile currentProfile = Profile.builder()
			.profileUrl("currentProfileURl")
			.name("currentProfileURl")
			.thumbnail(true)
			.profileType(ProfileType.CUSTOMIZATION)
			.build();

		UsersProfile usersProfile = UsersProfile.builder()
			.user(user1)
			.profile(currentProfile)
			.build();

		Profile changingProfile = Profile.builder()
			.thumbnail(false)
			.profileUrl("changingProfileURl")
			.name("changingProfileURl")
			.profileType(ProfileType.CUSTOMIZATION)
			.build();

		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));
		given(usersProfileRepository.findByUserNicknameAndProfileThumbnailTrue(anyString())).willReturn(
			Optional.of(usersProfile));
		given(profileRepository.findProfileByProfileUrl(anyString())).willReturn(Optional.of(changingProfile));

		userInfoService.changeProfile("testNickname", profileRequestDto);

		verify(usersProfileRepository, times(0)).delete(usersProfile);
		verify(usersProfileRepository, times(0)).save(any(UsersProfile.class));
		assertThat(currentProfile.getThumbnail()).isFalse();
		assertThat(changingProfile.getThumbnail()).isTrue();
	}

	@Test
	@DisplayName("프로필 변경 성공 - BASIC -> CUSTOM")
	void testChangeProfile2() {
		ProfileRequestDto profileRequestDto = ProfileRequestDto.builder()
			.profileUrl("changingProfileURl")
			.build();

		Profile currentProfile = Profile.builder()
			.profileUrl("currentProfileURl")
			.name("currentProfileURl")
			.thumbnail(true)
			.profileType(ProfileType.BASIC)
			.build();

		UsersProfile usersProfile = UsersProfile.builder()
			.user(user1)
			.profile(currentProfile)
			.build();

		Profile changingProfile = Profile.builder()
			.thumbnail(false)
			.profileUrl("changingProfileURl")
			.name("changingProfileURl")
			.profileType(ProfileType.CUSTOMIZATION)
			.build();

		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));
		given(usersProfileRepository.findByUserNicknameAndProfileThumbnailTrue(anyString())).willReturn(
			Optional.of(usersProfile));
		given(profileRepository.findProfileByProfileUrl(anyString())).willReturn(Optional.of(changingProfile));

		userInfoService.changeProfile("testNickname", profileRequestDto);

		verify(usersProfileRepository, times(1)).delete(usersProfile);
		verify(usersProfileRepository, times(0)).save(any(UsersProfile.class));
		assertThat(currentProfile.getThumbnail()).isFalse();
		assertThat(changingProfile.getThumbnail()).isTrue();
	}

	@Test
	@DisplayName("프로필 변경 성공 - CUSTOM -> BASIC")
	void testChangeProfile3() {
		ProfileRequestDto profileRequestDto = ProfileRequestDto.builder()
			.profileUrl("changingProfileURl")
			.build();

		Profile currentProfile = Profile.builder()
			.profileUrl("currentProfileURl")
			.name("currentProfileURl")
			.thumbnail(true)
			.profileType(ProfileType.CUSTOMIZATION)
			.build();

		UsersProfile usersProfile = UsersProfile.builder()
			.user(user1)
			.profile(currentProfile)
			.build();

		Profile changingProfile = Profile.builder()
			.thumbnail(false)
			.profileUrl("changingProfileURl")
			.name("changingProfileURl")
			.profileType(ProfileType.BASIC)
			.build();

		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user1));
		given(usersProfileRepository.findByUserNicknameAndProfileThumbnailTrue(anyString())).willReturn(
			Optional.of(usersProfile));
		given(profileRepository.findProfileByProfileUrl(anyString())).willReturn(Optional.of(changingProfile));

		userInfoService.changeProfile("testNickname", profileRequestDto);

		verify(usersProfileRepository, times(0)).delete(usersProfile);
		verify(usersProfileRepository, times(1)).save(any(UsersProfile.class));
		assertThat(currentProfile.getThumbnail()).isFalse();
		assertThat(changingProfile.getThumbnail()).isTrue();
	}
}




