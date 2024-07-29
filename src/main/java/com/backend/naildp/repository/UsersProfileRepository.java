package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.ProfileType;
import com.backend.naildp.entity.UsersProfile;

public interface UsersProfileRepository extends JpaRepository<UsersProfile, Long> {

	@Query("select p.profileUrl from UsersProfile up join up.profile p where up.user.nickname = :nickname and p.thumbnail = true")
	Optional<String> findProfileUrlByNicknameAndThumbnailTrue(@Param("nickname") String nickname);

	@Query("select p.profileUrl from UsersProfile up join up.profile p where up.user.nickname = :nickname and p.profileType = :type and p.thumbnail = false")
	List<String> findProfileUrlsByNicknameAndType(@Param("nickname") String nickname,
		@Param("type") ProfileType profileType);

	@Query("select up from UsersProfile up where up.user.nickname = :nickname and up.profile.thumbnail = true")
	Optional<UsersProfile> findProfileByNicknameAndThumbnailTrue(@Param("nickname") String nickname);
}
