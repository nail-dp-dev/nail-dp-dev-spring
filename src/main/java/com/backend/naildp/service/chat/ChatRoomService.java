package com.backend.naildp.service.chat;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.RoomType;
import com.backend.naildp.dto.chat.ChatListResponse;
import com.backend.naildp.dto.chat.ChatListSummaryResponse;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.RenameChatRoomRequestDto;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.ChatRoom;
import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ChatRoomMapping;
import com.backend.naildp.repository.ChatRoomRepository;
import com.backend.naildp.repository.ChatRoomUserRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {
	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final ChatRoomStatusService chatRoomStatusService;

	@Transactional
	public UUID createChatRoom(String myNickname, ChatRoomRequestDto chatRoomRequestDto) {
		// 1:1 채팅일 경우
		chatRoomRequestDto.getNickname().add(myNickname);
		int participantCnt = chatRoomRequestDto.getNickname().size();
		List<String> userNames = chatRoomRequestDto.getNickname();
		Optional<ChatRoom> duplicatedChatRoom;
		if (participantCnt == 2) {
			duplicatedChatRoom = chatRoomRepository.findChatRoomByUsers(userNames, userNames.size());
		} else {
			duplicatedChatRoom = chatRoomRepository.findMostRecentChatRoomByDuplicatedGroup(userNames);

		}
		// 이미 존재한다면
		if (duplicatedChatRoom.isPresent()) {
			ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(
					duplicatedChatRoom.get()
						.getId(), myNickname)
				.orElseThrow(() -> new CustomException("채팅방 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
			// chatRoomUser.setIsExited(false);
			return duplicatedChatRoom.get().getId();
		}

		ChatRoom chatRoom = new ChatRoom();
		chatRoomRepository.save(chatRoom);

		if (participantCnt == 2) {
			chatRoom.updateRoomType(RoomType.PERSONAL);
		} else {
			chatRoom.updateRoomType(RoomType.GROUP);
		}

		chatRoom.updateParticipantCnt(participantCnt);

		chatRoomRequestDto.getNickname().forEach(participant -> {
			User user = userRepository.findByNickname(participant)
				.orElseThrow(() -> new CustomException("해당 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

			String roomName = chatRoomRequestDto.getNickname()
				.stream()
				.filter(nickname -> !nickname.equals(participant))
				.collect(Collectors.joining(", "));

			ChatRoomUser chatRoomUser = new ChatRoomUser(user, chatRoom);
			chatRoomUserRepository.save(chatRoomUser);
			chatRoomUser.updateRoomName(roomName);
		});
		return chatRoom.getId();
	}

	@Transactional(readOnly = true)
	public ChatListSummaryResponse getChatList(String nickname, String category, int size, UUID cursorId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<ChatRoomMapping> chatRoomList;

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		if (cursorId == null) {
			chatRoomList = chatRoomRepository.findAllChatRoomByNicknameAndCategory(nickname, category, pageRequest);
		} else {
			chatRoomList = chatRoomRepository.findAllChatRoomByNicknameAndCategoryAndId(nickname, category,
				cursorId,
				pageRequest);

		}
		if (chatRoomList.isEmpty()) {
			return ChatListSummaryResponse.createEmptyResponse();
		}

		List<ChatListResponse> chatRoomDto = chatRoomList.stream()
			.map(chatRoom -> {
				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoom.getId().toString(),
					user.getNickname());
				List<String> profileUrls = chatRoomRepository.findOtherUsersThumbnailUrls(chatRoom.getId(),
					nickname);
				return ChatListResponse.of(chatRoom, unreadCount, profileUrls);
			})
			.collect(Collectors.toList());

		return ChatListSummaryResponse.of(new SliceImpl<>(chatRoomDto, pageRequest, chatRoomList.hasNext()));

	}

	@Transactional
	public void leaveChatRoom(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(
				chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		chatRoomUser.setIsExited(true);
		// 단체 채팅방만
		if (!chatRoom.isPersonal()) {
			// 방 이름 업데이트
			List<ChatRoomUser> remainingUsers = chatRoomUserRepository.findAllByChatRoomIdAndIsExitedFalse(
				chatRoomId);

			chatRoom.updateParticipantCnt(remainingUsers.size());

			if (remainingUsers.size() == 1) {
				remainingUsers.get(0).updateRoomName("대화 상대 없음");
			} else {

				for (ChatRoomUser remainingUser : remainingUsers) {
					String updatedRoomName = remainingUsers.stream()
						.filter(user -> !user.getUser().getNickname().equals(remainingUser.getUser().getNickname()))
						.map(user -> user.getUser().getNickname())
						.collect(Collectors.joining(", "));

					remainingUser.updateRoomName(updatedRoomName);
				}
			}

			if (remainingUsers.isEmpty()) {
				chatRoomRepository.deleteById(chatRoomId);
			}
		}
	}

	@Transactional
	public void pinByChatRoomUser(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(
				chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUser.updatePinning(true);
	}

	@Transactional
	public void unpinByChatRoomUser(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(
				chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUser.updatePinning(false);
	}

	@Transactional
	public void renameChatRoom(UUID chatRoomId, String nickname, RenameChatRoomRequestDto request) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(
				chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));

		chatRoomUser.updateRoomName(request.getChatRoomName());
	}

	@Transactional(readOnly = true)
	public List<SearchUserResponse> getRecommendUsers(String nickname) {
		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		return userRepository.findRecommendedUser(user);
	}

	public List<SearchUserResponse> getRecentUsers(String nickname) {
		List<String> recentUsers = chatRoomStatusService.getRecentUsers(nickname);
		List<SearchUserResponse> userInfoList = userRepository.findUserInfoByRecentUsers(nickname, recentUsers);

		Map<String, SearchUserResponse> userInfoMap = userInfoList.stream()
			.collect(Collectors.toMap(SearchUserResponse::getNickname, Function.identity()));

		return recentUsers.stream()
			.map(userInfoMap::get)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	public ChatListSummaryResponse searchChatRoomsByName(String nickname, String keyword, Pageable pageable,
		UUID cursorId) {
		Slice<ChatListResponse> chatRooms = chatRoomRepository.searchChatRoomsByName(keyword, pageable, cursorId,
			nickname);

		if (chatRooms.isEmpty()) {
			return ChatListSummaryResponse.createEmptyResponse();
		}

		return ChatListSummaryResponse.of(chatRooms);
	}
}