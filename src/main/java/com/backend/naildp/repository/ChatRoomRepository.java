package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
		"SELECT cu.chatRoom.id AS id, cu.name AS name, cu.chatRoom.lastMessage AS lastMessage, cu.chatRoom.participantCnt as participantCnt, cu.chatRoom.lastModifiedDate AS modifiedAt"
			+ " FROM ChatRoomUser cu "
			+ "WHERE cu.user.nickname = :nickname "
			+ "ORDER BY cu.chatRoom.lastModifiedDate desc")
	List<ChatRoomMapping> findAllChatRoomByNickname(@Param("nickname") String nickname);

	@Query(
		"SELECT cu.chatRoom.id AS id, cu.name AS name, cu.chatRoom.lastMessage AS lastMessage, cu.chatRoom.participantCnt AS participantCnt, cu.chatRoom.lastModifiedDate AS modifiedAt, cu.isPinning AS isPinning "
			+ "FROM ChatRoomUser cu "
			+ "WHERE cu.user.nickname = :nickname "
			+ "AND cu.isExited = false "
			+ "AND (cu.isPinning = true "
			+ "OR (:category = 'all' OR (:category = 'personal' AND cu.chatRoom.roomType = 'PERSONAL') "
			+ "OR (:category = 'group' AND cu.chatRoom.roomType = 'GROUP'))) "
			+ "ORDER BY cu.isPinning DESC, cu.chatRoom.lastModifiedDate DESC"
	)
	Slice<ChatRoomMapping> findAllChatRoomByNicknameAndCategory(
		@Param("nickname") String nickname,
		@Param("category") String category, PageRequest pageRequest
	);

	@Query(
		"SELECT cu.chatRoom.id AS id, cu.name AS name, cu.chatRoom.lastMessage AS lastMessage, cu.chatRoom.participantCnt AS participantCnt, cu.chatRoom.lastModifiedDate AS modifiedAt, cu.isPinning AS isPinning "
			+ "FROM ChatRoomUser cu "
			+ "WHERE cu.user.nickname = :nickname "
			+ "AND cu.isExited = false "
			+ "AND (cu.isPinning = false "
			+ "AND cu.chatRoom.lastModifiedDate < (SELECT c.lastModifiedDate FROM ChatRoom c WHERE c.id = :cursorId) "
			+ "AND (:category = 'all' OR (:category = 'personal' AND cu.chatRoom.roomType = 'PERSONAL') "
			+ "OR (:category = 'group' AND cu.chatRoom.roomType = 'GROUP'))) "
			+ "ORDER BY cu.isPinning DESC, cu.chatRoom.lastModifiedDate DESC"
	)
	Slice<ChatRoomMapping> findAllChatRoomByNicknameAndCategoryAndId(
		@Param("nickname") String nickname,
		@Param("category") String category, @Param("cursorId") UUID cursorId, PageRequest pageRequest
	);

	@Query("SELECT u.thumbnailUrl FROM ChatRoomUser cu JOIN cu.user u WHERE cu.chatRoom.id = :chatRoomId AND u.nickname != :nickname")
	List<String> findOtherUsersThumbnailUrls(@Param("chatRoomId") UUID chatRoomId, @Param("nickname") String nickname);
}
