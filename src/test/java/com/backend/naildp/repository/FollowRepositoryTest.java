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
		user1 = createMember("mj");
		user2 = createMember("jw");

		createFollow(user1, user2);
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
		User user = createMember("user");
		User follower1 = createMember("follower1");
		User follower2 = createMember("follower2");
		User follower3 = createMember("follower3");
		User follower4 = createMember("follower4");

		createFollow(follower1, user);
		createFollow(follower2, user);
		createFollow(follower3, user);
		createFollow(follower4, user);

		//when
		List<User> followerUsers = em.createQuery(
				"select f.follower from Follow f where f.following.nickname = :nickname",
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
		User user = createMember("user");
		User following1 = createMember("following1");
		User following2 = createMember("following2");
		User following3 = createMember("following3");
		User following4 = createMember("following4");

		createFollow(user, following1);
		createFollow(user, following2);
		createFollow(user, following3);
		createFollow(user, following4);

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
		User user = createMember("user");
		int followingCnt = 10;
		for (int i = 0; i < followingCnt; i++) {
			User followingUser = createMember("following" + i);
			createFollow(user, followingUser);
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
		User user = createMember("user");
		int followerCnt = 10;
		for (int i = 0; i < followerCnt; i++) {
			User followerUser = createMember("follower" + i);
			createFollow(followerUser, user);
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
		User user = createMember("user");
		User notFollowingUser = createMember("notFollowingUser");
		User followingUser = createMember("following");
		createFollow(user, followingUser);

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
		User followerUser = createMember("follower");
		User notFollowingUser = createMember("notFollowingUser");
		User followingUser = createMember("following");
		createFollow(followerUser, followingUser);

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

	@DisplayName("팔로워 취소 테스트")
	@Test
	void deleteFollowByNicknames() {
		//given
		User follower = createMember("follower");
		User followee = createMember("following");
		createFollow(follower, followee);

		//when
		followRepository.deleteByFollowerNicknameAndFollowingNickname(follower.getNickname(),
			followee.getNickname());

		//then
		Optional<Follow> followOptional = followRepository.findFollowByFollowerNicknameAndFollowingNickname(
			follower.getNickname(),
			followee.getNickname());
		assertThat(followOptional.isPresent()).isFalse();
	}

	private User createMember(String nickname) {
		User user = User.builder()
			.nickname(nickname)
			.role(UserRole.USER)
			.phoneNumber("")
			.agreement(true)
			.thumbnailUrl("")
			.build();
		em.persist(user);
		return user;
	}

	private void createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}
}
