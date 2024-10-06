package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.backend.naildp.common.ProfileType;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UsersProfile;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsersProfileRepositoryTest {

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	UsersProfileRepository usersProfileRepository;

	@Autowired
	EntityManager em;

	private User user1;
	private User user2;

	private Profile profile1;
	private Profile profile2;

	private UsersProfile userProfile1;
	private UsersProfile userProfile2;

	@BeforeEach
	void setup() {
		user1 = createTestMember("mj@naver.com", "mj", "0101111", 1L);
		user2 = createTestMember("jw@naver.com", "jw", "0102222", 2L);

		profile1 = createTestProfile("mjProfileUrl.jpg", "mjName", ProfileType.CUSTOMIZATION);
		profile2 = createTestProfile("jwProfile.jpg", "jwName", ProfileType.CUSTOMIZATION);

		userProfile1 = createTestUsersProfile(user1, profile1);
		userProfile2 = createTestUsersProfile(user2, profile2);

		em.clear();

	}

	@DisplayName("해당 유저의 썸네일이 설정된 프로필 찾기")
	@Test
	void testFindProfileUrlByThumbnailIsTrueAndUser() {
		// Given
		String profileUrl1 = "mjProfileUrl.jpg";
		String profileUrl2 = "jwProfile.jpg";

		// When
		Optional<Profile> foundProfile1 = profileRepository.findProfileByProfileUrl(
			profileUrl1);
		Optional<Profile> foundProfile2 = profileRepository.findProfileByProfileUrl("NotExist.jpg");
		// Then
		assertThat(foundProfile1).isPresent();
		assertThat(foundProfile1.get().getProfileUrl()).isEqualTo("mjProfileUrl.jpg");

		assertThat(foundProfile2).isNotPresent();
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		User user = User.builder()
			.nickname(nickname)
			.role(UserRole.USER)
			.phoneNumber(phoneNumber)
			.agreement(true)
			.build();

		em.persist(user);
		return user;
	}

	private Profile createTestProfile(String url, String name, ProfileType profileType) {
		Profile profile = Profile.builder()
			.profileUrl(url)
			.name(name)
			.profileType(profileType)
			.build();
		em.persist(profile);
		return profile;
	}

	private UsersProfile createTestUsersProfile(User user, Profile profile) {
		UsersProfile usersProfile = UsersProfile.builder()
			.user(user)
			.profile(profile)
			.build();
		em.persist(usersProfile);
		return usersProfile;
	}
}