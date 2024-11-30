package com.backend.naildp.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.common.converter.AESUtil;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;

@ActiveProfiles(profiles = {"test", "secret"})
@SpringBootTest
@Transactional
class UserRepositoryTest {

	@Autowired
	AESUtil aesUtil;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EntityManager em;

	@Test
	void saveEncryptPhoneNumber() {
		//given
		User user = User.builder()
			.nickname("nickname")
			.phoneNumber("01012341234")
			.thumbnailUrl("")
			.role(UserRole.USER)
			.agreement(true)
			.build();
		userRepository.saveAndFlush(user);

		em.clear();

		//when
		User findUser = em.createQuery("select u from Users u where u.phoneNumber = :phoneNumber", User.class)
			.setParameter("phoneNumber", user.getPhoneNumber())
			.getSingleResult();

		//then
		Assertions.assertThat(user.getPhoneNumber()).isEqualTo(findUser.getPhoneNumber());
	}
}