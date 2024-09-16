package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.common.ProviderType;
import com.backend.naildp.entity.SocialLogin;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, String> {

	Optional<UserMapping> findBySocialIdAndPlatform(String socialId, ProviderType platform);

}