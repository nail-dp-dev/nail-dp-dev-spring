package com.backend.naildp.repository;

import static com.backend.naildp.entity.QArchivePost.*;
import static com.backend.naildp.entity.QFollow.*;
import static com.backend.naildp.entity.QPost.*;
import static com.backend.naildp.entity.QUser.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfiguration.class, QueryDslTestConfig.class})
public class UserSearchRepositoryTest {

	@Autowired
	JPAQueryFactory jpaQueryFactory;

	@PersistenceContext
	EntityManager em;

	@Autowired
	UserRepository userRepository;

	final String NICKNAME_PREFIX = "x";
	final int SEARCH_USER_CNT = 20;
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
		User 가나다라 = createUserByNickname("가나다라");
		User 나가 = createUserByNickname("나가");
		User 나가라 = createUserByNickname("나가라");

		User 다나가라 = createUserByNickname("다나가라");
		createFollow(user, 다나가라);
		User 나가지마라 = createUserByNickname("나가지마라");
		createFollow(user, 나가지마라);

		em.flush();
		em.clear();
	}

	@DisplayName("나 를 포함하는 사용자 검색 테스트")
	@Test
	void searchUserContainingKeyword() {
		String keyword = "나";
		String nickname = "nickname";

		List<SearchUserResponse> responses = userRepository.searchByKeyword(keyword, nickname);

		for (SearchUserResponse res : responses) {
			System.out.println("res = " + res.getNickname());
		}
	}

	@DisplayName("키워드로 사용자 검색 테스트")
	@Test
	void searchUserByKeyword() {
		//given
		String keyword = NICKNAME_PREFIX;
		String username = "nickname";

		//when
		List<SearchUserResponse> responses = userRepository.searchByKeyword(keyword, username);

		//then
		assertThat(responses).hasSize(10);
	}

	@DisplayName("사용자 닉네임이 아닌 키워드로 검색")
	@Test
	void searchUserByNotExistedKeyword() {
		//given
		String keyword = "abcdefghijklmnopqrstuvwxyz";
		String username = "nickname";

		//when
		List<SearchUserResponse> responses = userRepository.searchByKeyword(keyword, username);

		//then
		assertThat(responses).hasSize(0);
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
