package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.UsersProfile;

public interface UsersProfileRepository extends JpaRepository<UsersProfile, Long> {

	@Query("select p.profileUrl from UsersProfile up join up.profile p where up.user.nickname = :nickname and p.thumbnail = true")
	Optional<String> findProfileUrlByUserIdAndThumbnailTrue(@Param("nickname") String nickname);

}
