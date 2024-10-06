package com.backend.naildp.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

	Optional<User> findUserByNickname(String nickname);

	Optional<User> findByNickname(String nickname);

	Optional<User> findByPhoneNumber(String phoneNumber);

	Optional<User> findByLoginId(String loginId);

}
