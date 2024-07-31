package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.ProfileType;
import com.backend.naildp.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	@Query("select p.profileUrl from Profile p where p.thumbnail = false and p.profileType = :type")
	List<String> findProfileUrlsByType(@Param("type") ProfileType profileType);

	Optional<Profile> findProfileByProfileUrl(String profileUrl);
}
