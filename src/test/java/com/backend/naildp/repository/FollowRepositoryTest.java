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
	private Long cnt = 1L;

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

	@DisplayName("해당 유저가 팔로우한 유저목록 조회 테스트")
	@Test
	void findFollowingListByFollowerNickname() {
		//given
		User follower = createTestMember("follower@naver.com", "follower", "010", cnt);
		User followingUser1 = createTestMember("following1@naver.com", "following1", "010", cnt);
		User followingUser2 = createTestMember("following2@naver.com", "following2", "010", cnt);
		User followingUser3 = createTestMember("following3@naver.com", "following3", "010", cnt);
		User followingUser4 = createTestMember("following4@naver.com", "following4", "010", cnt);
		createTestFollow(follower, followingUser1);
		createTestFollow(follower, followingUser2);
		createTestFollow(follower, followingUser3);

		//when
		List<User> followingUserByFollowerNickname = followRepository.findFollowingUserByFollowerNickname(
			follower.getNickname());

		//then
		assertThat(followingUserByFollowerNickname).extracting("nickname")
			.containsExactly(followingUser1.getNickname(), followingUser2.getNickname(), followingUser3.getNickname());
		assertThat(followingUserByFollowerNickname).extracting("nickname").doesNotContain(followingUser4.getNickname());
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, phoneNumber, true);
		User user = new User(loginRequestDto, UserRole.USER);
		SocialLogin socialLogin = new SocialLogin(socialLoginId, "kakao", email, user);
		em.persist(user);
		em.persist(socialLogin);
		cnt++;
		return user;
	}

	private void createTestFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}
}
