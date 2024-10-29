package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
	List<ChatRoomUser> findAllByUser(User user);

	List<ChatRoomUser> findAllByChatRoomIdAndIsExitedFalse(UUID chatRoomId);

	Optional<ChatRoomUser> findByChatRoomIdAndUserNickname(UUID chatRoomId, String nickname);

	Optional<ChatRoomUser> findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(UUID chatRoomId, String nickname);

	List<ChatRoomUser> findAllByChatRoomId(@Param("chatRoomId") UUID chatRoomId);

}

