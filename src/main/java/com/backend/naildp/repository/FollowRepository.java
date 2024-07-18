package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	@Query("select f.following.nickname from Follow f where f.follower.nickname=:nickname")
	List<String> findFollowingNicknamesByUserNickname(@Param("nickname") String nickname);

	@Query("select count(f) from Follow f where f.following.nickname=:nickname")
	int countFollowersByUserNickname(@Param("nickname") String nickname);
}
