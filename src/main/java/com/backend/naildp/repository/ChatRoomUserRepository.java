package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
	List<ChatRoomUser> findAllByUser(User user);

	List<ChatRoomUser> findAllByChatRoomId(UUID chatRoomId);

	Optional<ChatRoomUser> findByChatRoomIdAndUserNickname(UUID chatRoomId, String nickname);

	@Query("SELECT cu.user.nickname FROM ChatRoomUser cu WHERE cu.chatRoom.id = :chatRoomId AND cu.user.nickname != :nickname")
	List<String> findAllByChatRoomIdAndNotMe(@Param("chatRoomId") UUID chatRoomId,
		@Param("nickname") String nickname);

}

