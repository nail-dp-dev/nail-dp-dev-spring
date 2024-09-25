package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.ProfileType;
import com.backend.naildp.entity.UsersProfile;

public interface UsersProfileRepository extends JpaRepository<UsersProfile, Long> {

	// @Query("select p.profileUrl from UsersProfile up join up.profile p where up.user.nickname = :nickname and p.thumbnail = true")
	// Optional<String> findProfileUrlByNicknameAndThumbnailTrue(@Param("nickname") String nickname);

	@Query("select p.profileUrl from UsersProfile up join up.profile p where up.user.nickname = :nickname and p.profileType = 'CUSTOMIZATION'")
	List<String> findProfileUrlsByNicknameAndCustomization(@Param("nickname") String nickname);

	// Optional<UsersProfile> findByUserNicknameAndProfileThumbnailTrue(@Param("nickname") String nickname);

	int countByUserNicknameAndProfileProfileType(String nickname, ProfileType profileType);

	Optional<UsersProfile> findFirstByUserNicknameAndProfileProfileType(String nickname,
		ProfileType profileType);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	void deleteByProfileIdAndUserNickname(Long profileId, String nickname);

}
