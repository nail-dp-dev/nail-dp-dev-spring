package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.config.QueryDslTestConfig;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfiguration.class, QueryDslTestConfig.class})
public class UserSearchRepositoryTest {

	@Autowired
	JPAQueryFactory jpaQueryFactory;

	@Autowired
	EntityManager em;

	@Autowired
	UserRepository userRepository;

	final String NICKNAME_PREFIX = "x";
	final int SEARCH_USER_CNT = 50;
	final int POST_CNT = 10;

	@BeforeEach
	void before() {
		User user = createUserByNickname("nickname");
		for (int i = 1; i <= SEARCH_USER_CNT; i++) {
			User searchUser = createUserByNickname(NICKNAME_PREFIX + i);
			if (i % 10 == 0) {
				createFollow(user, searchUser);
			}
			savePostByCnt(searchUser, i + 1);
		}

		em.flush();
		em.clear();
	}

	// @Disabled
	@Test
	void test() {
		//given
		String keyword = NICKNAME_PREFIX;
		String username = "nickname";

		//when
		List<SearchUserResponse> responses = userRepository.searchByKeyword(keyword, username);

		//then
		// for (SearchUserResponse res : responses) {
		// 	System.out.println("============");
		// 	System.out.println("res.Nickname = " + res.getNickname());
		// 	System.out.println("res.ProfileUrl = " + res.getProfileUrl());
		// 	System.out.println("res.PostCount = " + res.getPostCount());
		// 	System.out.println("res.savedPostCount = " + res.getSavedPostCount());
		// 	System.out.println("res.FollowerCount = " + res.getFollowerCount());
		// 	System.out.println("res.IsFollowing = " + res.isFollowing());
		// }
		assertThat(responses).hasSize(10);
	}

	private User createUserByNickname(String nickname) {
		User user = User.builder()
			.nickname(nickname)
			.phoneNumber("pn")
			.agreement(true)
			.role(UserRole.USER)
			.build();
		em.persist(user);
		return user;
	}

	private Post createPostByUser(User user) {
		Post post = Post.builder()
			.user(user)
			.postContent("")
			.tempSave(false)
			.boundary(Boundary.ALL)
			.build();
		em.persist(post);
		return post;
	}

	private void savePostByCnt(User x, int postCnt) {
		for (int i = 0; i < postCnt; i++) {
			createPostByUser(x);
		}
	}

	private void createFollow(User follower, User following) {
		Follow follow = new Follow(follower, following);
		em.persist(follow);
	}
}
