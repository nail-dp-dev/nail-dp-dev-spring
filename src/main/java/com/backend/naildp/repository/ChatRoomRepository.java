package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
	@Query(
		"SELECT cu.chatRoom FROM ChatRoomUser cu " + "WHERE cu.user.nickname IN :userNames " + "GROUP BY cu.chatRoom "
			+ "HAVING COUNT(DISTINCT cu.user.id) = :size ")
	Optional<ChatRoom> findChatRoomByUsers(@Param("userNames") List<String> userNames, @Param("size") int size);

	@Query(
		"SELECT cu.chatRoom.id AS id, cu.name AS name, cu.chatRoom.lastMessage AS lastMessage, cu.chatRoom.participantCnt as participantCnt, cu.chatRoom.modifiedAt AS modifiedAt"
			+ " FROM ChatRoomUser cu "
			+ "WHERE cu.user.nickname = :nickname "
			+ "ORDER BY cu.chatRoom.modifiedAt desc")
	List<ChatRoomMapping> findAllChatRoomByNickname(@Param("nickname") String nickname);

	@Query("SELECT u.thumbnailUrl FROM ChatRoomUser cu JOIN cu.user u WHERE cu.chatRoom.id = :chatRoomId AND u.nickname != :nickname")
	List<String> findOtherUsersThumbnailUrls(@Param("chatRoomId") UUID chatRoomId, @Param("nickname") String nickname);
}
