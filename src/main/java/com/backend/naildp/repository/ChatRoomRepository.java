package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.backend.naildp.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
	@Query(
		"SELECT cu.chatRoom FROM ChatRoomUser cu " + "WHERE cu.user.nickname IN :userNames " + "GROUP BY cu.chatRoom "
			+ "HAVING COUNT(DISTINCT cu.user.id) = :size ")
	Optional<ChatRoom> findChatRoomByUsers(List<String> userNames, int size);
}
