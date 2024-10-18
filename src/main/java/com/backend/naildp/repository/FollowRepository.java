package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	@Query("select f.following.nickname from Follow f where f.follower.nickname=:nickname")
	List<String> findFollowingNicknamesByUserNickname(@Param("nickname") String nickname);

	@Query("select f.following from Follow f where f.follower.nickname = :followerNickname")
	List<User> findFollowingUserByFollowerNickname(@Param("followerNickname") String nickname);

	@Query("select count(f) from Follow f where f.following.nickname=:nickname")
	int countFollowersByUserNickname(@Param("nickname") String nickname);

	@Query("select count(f) from Follow f where f.follower.nickname=:nickname")
	int countFollowingsByUserNickname(@Param("nickname") String nickname);

	boolean existsByFollowerNicknameAndFollowing(String nickname, User writer);

	Optional<Follow> findFollowByFollowerNicknameAndFollowingNickname(String followerNickname, String followingNickname);

	void deleteByFollowerNicknameAndFollowingNickname(String followerNickname, String followingNickname);
}
