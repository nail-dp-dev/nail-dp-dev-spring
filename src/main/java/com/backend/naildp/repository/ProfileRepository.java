package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.User;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	Optional<Profile> findProfileUrlByThumbnailIsTrueAndUser(User user);
}
