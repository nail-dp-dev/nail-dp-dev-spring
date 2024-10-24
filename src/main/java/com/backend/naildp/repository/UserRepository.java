package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

	Optional<User> findUserByNickname(String nickname);

	Optional<User> findByNickname(String nickname);

	Optional<User> findByPhoneNumber(String phoneNumber);

	@Query("SELECT u FROM ChatRoomUser cu JOIN cu.user u WHERE cu.chatRoom.id = :chatRoomId AND u.nickname != :nickname AND cu.isExited = false")
	List<User> findAllByChatRoomIdNotInMyNickname(@Param("chatRoomId") UUID chatRoomId,
		@Param("nickname") String nickname);
}
