package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByNickname(String nickname);

	Optional<User> findByPhoneNumber(String phoneNumber);

}
