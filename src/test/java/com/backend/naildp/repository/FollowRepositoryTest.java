package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FollowRepositoryTest {

	@Autowired
	FollowRepository followRepository;

	@Autowired
	EntityManager em;

	private User user1;
	private User user2;

	@BeforeEach
	void setup() {
		user1 = createTestMember("mj@naver.com", "mj", "0101111", 1L);
		user2 = createTestMember("jw@naver.com", "jw", "0102222", 2L);

		createTestFollow(user1, user2);
		em.clear();
	}

	@DisplayName("해당 유저의 팔로잉 찾기")
	@Test
	void testFindFollowingNicknamesByUserNickname() {

		// When
		List<String> followingNicknames = followRepository.findFollowingNicknamesByUserNickname("mj");

		// Then
		assertThat(followingNicknames).contains("jw");
	}

	@DisplayName("해당 유저의 팔로워 수")
	@Test
	void testCountFollowsByFollowerNickname() {
		// When
		int followCount = followRepository.countFollowersByUserNickname("mj");

		// Then
		assertThat(followCount).isEqualTo(0);
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLoginId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		return user;
	}

	private void createTestFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}
}
