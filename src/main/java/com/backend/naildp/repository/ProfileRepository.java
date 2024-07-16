package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.User;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	Profile findProfileUrlByThumbnailIsTrueAndUser(User user);
}
