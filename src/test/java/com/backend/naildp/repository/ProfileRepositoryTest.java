package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileRepositoryTest {

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	EntityManager em;

	private User user1;
	private User user2;

	private Profile user1Profile;
	private Profile user2Profile;

	@BeforeEach
	void setup() {
		user1 = createTestMember("mj@naver.com", "mj", "0101111", 1L);
		user2 = createTestMember("jw@naver.com", "jw", "0102222", 2L);

		user1Profile = createTestProfile(user1, "mjProfileUrl.jpg", "mjName", true);
		user2Profile = createTestProfile(user2, "jwProfile.jpg", "jwName", false);

		em.clear();
	}

	@DisplayName("해당 유저의 썸네일이 설정된 프로필 찾기")
	@Test
	void testFindProfileUrlByThumbnailIsTrueAndUser() {
		// Given

		// When
		Profile foundProfile1 = profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user1);
		Profile foundProfile2 = profileRepository.findProfileUrlByThumbnailIsTrueAndUser(user2);

		// Then
		assertThat(foundProfile1).isNotNull();
		assertThat(foundProfile1.getProfileUrl()).isEqualTo("mjProfileUrl.jpg");

		assertThat(foundProfile2).isNull();
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLoginId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		return user;
	}

	private Profile createTestProfile(User user, String url, String name, boolean thumb) {
		Profile profile = new Profile(user, url, name, thumb);
		em.persist(profile);
		return profile;
	}
}