package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.ChatRoom;
import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
	List<ChatRoomUser> findAllByUser(User user);

	@Query(
		"SELECT cu.chatRoom FROM ChatRoomUser cu " + "WHERE cu.user.nickname IN :userNames " + "GROUP BY cu.chatRoom "
			+ "HAVING COUNT(DISTINCT cu.user.id) = :size ")
	Optional<ChatRoom> findChatRoomByUsers(@Param("userNames") List<String> userNames, @Param("size") int size);

	@Query(
		"SELECT cu.chatRoom.id as id, cu.name as name FROM ChatRoomUser cu " + "WHERE cu.user.nickname = :nickname ")
	List<ChatRoomMapping> findAllChatRoomByNickname(@Param("nickname") String nickname);
}
