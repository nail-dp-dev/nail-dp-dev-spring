package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@Import(JpaAuditingConfiguration.class)
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

	@DisplayName("팔로워 목록 조회")
	@Test
	void followerList() {
		//given
		User user = createTestMember("user@", "user", "010", cnt);
		User follower1 = createTestMember("follower1@", "follower1", "010", cnt);
		User follower2 = createTestMember("follower2@", "follower2", "010", cnt);
		User follower3 = createTestMember("follower3@", "follower3", "010", cnt);
		User follower4 = createTestMember("follower4@", "follower4", "010", cnt);

		createTestFollow(follower1, user);
		createTestFollow(follower2, user);
		createTestFollow(follower3, user);
		createTestFollow(follower4, user);

		//when
		List<User> followerUsers = em.createQuery("select f.follower from Follow f where f.following.nickname = :nickname",
				User.class)
			.setParameter("nickname", user.getNickname())
			.getResultList();

		//then
		assertThat(followerUsers).hasSize(4);
	}

	@DisplayName("팔로잉 목록 조회")
	@Test
	void followingList() {
		//given
		User user = createTestMember("user@", "user", "010", cnt);
		User following1 = createTestMember("following1@", "following1", "010", cnt);
		User following2 = createTestMember("following2@", "following2", "010", cnt);
		User following3 = createTestMember("following3@", "following3", "010", cnt);
		User following4 = createTestMember("following4@", "following4", "010", cnt);

		createTestFollow(user, following1);
		createTestFollow(user, following2);
		createTestFollow(user, following3);
		createTestFollow(user, following4);

		//when
		List<User> followingUsersByFollowerNickname = followRepository.findFollowingUserByFollowerNickname(
			user.getNickname());

		//then
		assertThat(followingUsersByFollowerNickname).hasSize(4);
	}

	@DisplayName("팔로잉 수 조회")
	@Test
	void countFollowingList() {
		//given
		User user = createTestMember("user@", "user", "010", cnt);
		int followingCnt = 10;
		for (int i = 0; i < followingCnt; i++) {
			User followingUser = createTestMember("following@", "following" + i, "010", cnt);
			createTestFollow(user, followingUser);
		}

		//when
		int countFollowingsByUserNickname = followRepository.countFollowingsByUserNickname(user.getNickname());

		//then
		assertThat(countFollowingsByUserNickname).isEqualTo(followingCnt);
	}

	@DisplayName("팔로워 수 조회")
	@Test
	void countFollowerList() {
		//given
		User user = createTestMember("user@", "user", "010", cnt);
		int followerCnt = 10;
		for (int i = 0; i < followerCnt; i++) {
			User followerUser = createTestMember("follower@", "follower" + i, "010", cnt);
			createTestFollow(followerUser, user);
		}

		//when
		int countFollowingsByUserNickname = followRepository.countFollowersByUserNickname(user.getNickname());

		//then
		assertThat(countFollowingsByUserNickname).isEqualTo(followerCnt);
	}

	@DisplayName("팔로잉 유무 확인 테스트")
	@Test
	void checkFollowingStatusByNickname() {
		//given
		User user = createTestMember("user@", "user", "010", cnt);
		User notFollowingUser = createTestMember("notFollowingUser@", "notFollowingUser", "010", cnt);
		User followingUser = createTestMember("following@", "following", "010", cnt);
		createTestFollow(user, followingUser);

		//when
		boolean followingStatusWithFollowingUser = followRepository.existsByFollowerNicknameAndFollowing(
			user.getNickname(), followingUser);
		boolean followingStatusWithNotFollowingUser = followRepository.existsByFollowerNicknameAndFollowing(
			user.getNickname(), notFollowingUser);

		//then
		assertThat(followingStatusWithFollowingUser).isTrue();
		assertThat(followingStatusWithNotFollowingUser).isFalse();
	}

	@DisplayName("팔로워 닉네임과 팔로잉 닉네임으로 팔로워 찾기")
	@Test
	void findFollowByNicknames() {
		//given
		User followerUser = createTestMember("follower@", "follower", "010", cnt);
		User notFollowingUser = createTestMember("notFollowingUser@", "notFollowingUser", "010", cnt);
		User followingUser = createTestMember("following@", "following", "010", cnt);
		createTestFollow(followerUser, followingUser);

		//when
		Optional<Follow> followedOptional = followRepository.findFollowByFollowerNicknameAndFollowingNickname(
			followerUser.getNickname(), followingUser.getNickname());
		Optional<Follow> unfollowedOptional = followRepository.findFollowByFollowerNicknameAndFollowingNickname(
			followerUser.getNickname(),
			notFollowingUser.getNickname());

		//then
		assertThat(followedOptional.isPresent()).isTrue();
		assertThat(unfollowedOptional.isPresent()).isFalse();
	}

	@Test
	void deleteFollowByNicknames() {
		//given
		User followerUser = createTestMember("follower@", "follower", "010", cnt);
		User followingUser = createTestMember("following@", "following", "010", cnt);
		createTestFollow(followerUser, followingUser);

		em.flush();
		em.clear();

		//when
		followRepository.deleteByFollowerNicknameAndFollowingNickname(followerUser.getNickname(),
			followingUser.getNickname());

		//then
		Optional<Follow> followOptional = followRepository.findFollowByFollowerNicknameAndFollowingNickname(
			followerUser.getNickname(),
			followingUser.getNickname());
		assertThat(followOptional.isPresent()).isFalse();
	}

	private User createTestMember(String email, String nickname, String phoneNumber, Long socialLoginId) {
		User user = User.builder()
			.nickname(nickname)
			.role(UserRole.USER)
			.phoneNumber(phoneNumber)
			.agreement(true)
			.build();

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
