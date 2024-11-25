package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.UserSubscription;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

	Optional<UserSubscription> findByUserNickname(String nickname);

	@Query("select us from UserNotification us where us.user.nickname = :nickname")
	Optional<UserSubscription> findSubscriptionAndUserByNickname(@Param("nickname") String nickname);

}
