package com.backend.naildp.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.backend.naildp.dto.chat.ChatListResponse;
import com.backend.naildp.entity.QChatRoom;
import com.backend.naildp.entity.QChatRoomUser;
import com.backend.naildp.entity.QUser;
import com.backend.naildp.service.chat.ChatRoomStatusService;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {
	private final JPAQueryFactory queryFactory;
	private final ChatRoomStatusService chatRoomStatusService;

	public ChatRoomRepositoryImpl(EntityManager em, ChatRoomStatusService chatRoomStatusService) {
		this.queryFactory = new JPAQueryFactory(em);
		this.chatRoomStatusService = chatRoomStatusService;
	}

	QChatRoom chatRoom = QChatRoom.chatRoom;
	QChatRoomUser chatRoomUser = QChatRoomUser.chatRoomUser;
	QUser user = QUser.user;

	@Override
	public Slice<ChatListResponse> searchChatRoomsByName(String keyword, Pageable pageable, UUID cursorId,
		String myNickname) {
		List<ChatListResponse> results = queryFactory
			.select(Projections.fields(ChatListResponse.class,
				chatRoomUser.name.as("roomName"),
				chatRoom.id.as("roomId"),
				chatRoom.lastMessage,
				chatRoom.participantCnt,
				chatRoom.lastModifiedDate.as("modifiedAt"),
				ExpressionUtils.as(Expressions.constant(false), "isBusiness"),  //business 계정이 생기면 수정 필요
				chatRoomUser.isPinning
			))
			.from(chatRoomUser)
			.join(chatRoomUser.chatRoom, chatRoom)
			.join(chatRoomUser.user, user)
			.where(
				user.nickname.eq(myNickname),
				chatRoomUser.name.containsIgnoreCase(keyword),
				cursorId == null ? null : chatRoom.id.gt(cursorId)
			)
			.orderBy(chatRoom.lastModifiedDate.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		Map<UUID, List<String>> profileUrlsMap = findProfileUrlsForChatRooms(results, myNickname);

		results.forEach(chatListResponse -> {
			UUID roomId = chatListResponse.getRoomId();
			chatListResponse.setProfileUrls(profileUrlsMap.get(roomId));

			int unreadCount = chatRoomStatusService.getUnreadCount(roomId.toString(), myNickname);
			chatListResponse.setUnreadCount(unreadCount);
		});

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private Map<UUID, List<String>> findProfileUrlsForChatRooms(List<ChatListResponse> results, String myNickname) {
		return results.stream()
			.map(ChatListResponse::getRoomId)
			.distinct()
			.collect(Collectors.toMap(
				roomId -> roomId,
				roomId -> queryFactory
					.select(user.thumbnailUrl)
					.from(user)
					.join(chatRoomUser).on(chatRoomUser.user.eq(user))
					.where(chatRoomUser.chatRoom.id.eq(roomId).and(user.nickname.ne(myNickname)))
					.fetch()
			));
	}
}