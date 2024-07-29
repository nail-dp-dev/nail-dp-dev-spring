package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.ProfileType;
import com.backend.naildp.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	@Query("select p.profileUrl from Profile p where p.thumbnail = true and p.profileType = :type")
	List<String> findProfileUrlsByType(@Param("type") ProfileType profileType);

	Optional<Profile> findProfileByProfileUrl(String profileUrl);

	// @Modifying
	// @Query("UPDATE Profile p SET p.thumbnail = false WHERE p.id = :profileId")
	// int updateProfileThumbnailToFalse(@Param("profileId") Long profileId);
	//
	// @Modifying
	// @Query("UPDATE Profile p SET p.thumbnail = true WHERE p.profileUrl = :profileUrl")
	// int updateProfileThumbnailToTrueByProfileUrl(@Param("profileUrl") String profileUrl);
}
