package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
	Optional<UserMapping> findBySocialIdAndPlatform(Long socialId, String platform);

	Optional<SocialLogin> findByUser(User user);
}