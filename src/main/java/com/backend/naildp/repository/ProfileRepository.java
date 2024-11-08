package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.ProfileType;
import com.backend.naildp.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	@Query("select p.profileUrl from Profile p where p.profileType = :type")
	List<String> findProfileUrlsByType(@Param("type") ProfileType profileType);

	Optional<Profile> findProfileByProfileUrl(String profileUrl);

	@Query("SELECT p FROM Profile p JOIN UsersProfile up ON p.id = up.profile.id WHERE up.user.nickname = :nickname AND p.profileUrl = :profileUrl")
	Optional<Profile> findProfileByNicknameAndProfileUrl(@Param("nickname") String nickname,
		@Param("profileUrl") String profileUrl);

	boolean existsProfileByProfileUrlStartsWith(String profileUrl);
}
