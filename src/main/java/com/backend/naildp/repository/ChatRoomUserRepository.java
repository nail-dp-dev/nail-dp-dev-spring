package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
	List<ChatRoomUser> findAllByUser(User user);

	List<ChatRoomUser> findAllByChatRoomId(UUID chatRoomId);

	Optional<ChatRoomUser> findByChatRoomIdAndUserNickname(UUID chatRoomId, String nickname);

}

