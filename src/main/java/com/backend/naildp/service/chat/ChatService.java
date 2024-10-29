package com.backend.naildp.service.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.RoomType;
import com.backend.naildp.dto.chat.ChatListResponse;
import com.backend.naildp.dto.chat.ChatListSummaryResponse;
import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.ChatUpdateDto;
import com.backend.naildp.dto.chat.MessageResponseDto;
import com.backend.naildp.dto.chat.MessageSummaryResponse;
import com.backend.naildp.dto.chat.RenameChatRoomRequestDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.ChatRoom;
import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.mongo.ChatMessage;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ChatRoomMapping;
import com.backend.naildp.repository.ChatRoomRepository;
import com.backend.naildp.repository.ChatRoomUserRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.mongo.ChatMessageRepository;
import com.backend.naildp.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomStatusService chatRoomStatusService;
	private final MessageStatusService messageStatusService;
	private final S3Service s3Service;
	private final SessionService sessionService;
	private final KafkaProducerService kafkaProducerService;

	@Transactional
	public UUID createChatRoom(String myNickname, ChatRoomRequestDto chatRoomRequestDto) {
		// 1:1 채팅일 경우
		chatRoomRequestDto.getNickname().add(myNickname);
		int participantCnt = chatRoomRequestDto.getNickname().size();

		if (participantCnt == 2) {
			List<String> userNames = Arrays.asList(myNickname, chatRoomRequestDto.getNickname().get(0));
			Optional<ChatRoom> chatRoom = chatRoomRepository.findChatRoomByUsers(userNames, userNames.size());
			// 이미 존재한다면
			if (chatRoom.isPresent()) {
				ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(
						chatRoom.get()
							.getId(), myNickname)
					.orElseThrow(() -> new CustomException("채팅방 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
				chatRoomUser.setIsExited(false);
				return chatRoom.get().getId();
			}
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

	@Transactional
	public void sendMessage(ChatMessageDto chatMessageDto, UUID chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		User user = userRepository.findByNickname(chatMessageDto.getSender())
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		long timestamp = System.currentTimeMillis();

		ChatMessage chatMessage = new ChatMessage(chatMessageDto, chatRoomId, user);
		chatMessageRepository.save(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		chatMessageDto.setChatRoomId(chatRoomId.toString());

		kafkaProducerService.send(chatMessageDto);

		List<String> nicknames = getChatRoomNickname(chatRoomId); // 방 참여자 목록
		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {
				boolean isActive = sessionService.isSessionExist(nickname);
				log.info(String.valueOf(isActive));
				if (!isActive) {
					chatRoomStatusService.incrementUnreadCount(chatRoomId.toString(), nickname);
					messageStatusService.setFirstUnreadMessageId(chatRoomId.toString(), nickname, chatMessage.getId());
				}
				chatRoomStatusService.addRecentUsers(chatMessageDto.getSender(), nickname, System.currentTimeMillis());
				chatRoomStatusService.addRecentUsers(nickname, chatMessageDto.getSender(), System.currentTimeMillis());

			}
		});

		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {

				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoomId.toString(),
					nickname);
				ChatUpdateDto chatUpdateDto = new ChatUpdateDto(chatRoomId, unreadCount, chatMessageDto.getContent(),
					LocalDateTime.now(), chatMessageDto.getSender(), nickname);

				kafkaProducerService.sendChatUpdate(chatUpdateDto);

			}
		});

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
			chatRoomList = chatRoomRepository.findAllChatRoomByNicknameAndCategoryAndId(nickname, category, cursorId,
				pageRequest);

		}
		if (chatRoomList.isEmpty()) {
			return ChatListSummaryResponse.createEmptyResponse();
		}

		List<ChatListResponse> chatRoomDto = chatRoomList.stream()
			.map(chatRoom -> {
				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoom.getId().toString(), user.getNickname());
				List<String> profileUrls = chatRoomRepository.findOtherUsersThumbnailUrls(chatRoom.getId(), nickname);
				return ChatListResponse.of(chatRoom, unreadCount, profileUrls);
			})
			.collect(Collectors.toList());

		return ChatListSummaryResponse.of(new SliceImpl<>(chatRoomDto, pageRequest, chatRoomList.hasNext()));

	}

	@Transactional(readOnly = true)
	public MessageSummaryResponse getMessagesByRoomId(UUID chatRoomId, String nickname) {
		List<MessageSummaryResponse.ChatUserInfoResponse> chatUserInfo;
		String firstUnreadMessageId = messageStatusService.getFirstUnreadMessageId(chatRoomId.toString(), nickname);

		List<ChatMessage> messages = chatMessageRepository.findAllByChatRoomId(chatRoomId.toString());
		List<MessageResponseDto> messageDto = messages.stream().map(message -> {
			Long unreadUserCount = messageStatusService.getUnreadUserCount(chatRoomId.toString(), message.getId());
			return MessageResponseDto.of(message, unreadUserCount);
		}).collect(Collectors.toList());

		List<User> roomUsers = userRepository.findAllByChatRoomIdNotInMyNickname(chatRoomId, nickname);
		if (roomUsers.isEmpty()) { // 단체 채팅방 모두 나간 경우(본인 제외)
			User user = userRepository.findByNickname(nickname)
				.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", ErrorCode.NOT_FOUND));

			boolean isActive = sessionService.isSessionExist(user.getNickname());
			chatUserInfo = List.of(new MessageSummaryResponse.ChatUserInfoResponse(
				user.getNickname(), user.getThumbnailUrl(), isActive, false));
		} else {

			chatUserInfo = roomUsers.stream().map(user -> {
				boolean isActive = sessionService.isSessionExist(user.getNickname());
				return new MessageSummaryResponse.ChatUserInfoResponse(user.getNickname(), user.getThumbnailUrl(),
					isActive,
					false);
			}).toList();
		}
		return new MessageSummaryResponse(messageDto, firstUnreadMessageId, chatUserInfo);
	}

	@Transactional
	public void sendImageMessages(UUID chatRoomId, String sender, List<MultipartFile> imageFiles) {
		String imageMessage = "사진을 보냈습니다";

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		List<FileRequestDto> fileRequestDtos = s3Service.saveFiles(imageFiles);
		List<String> imageUrls = fileRequestDtos.stream().map(FileRequestDto::getFileUrl).collect(Collectors.toList());

		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoomId(chatRoomId.toString())
			.sender(sender)
			.profileUrl(user.getThumbnailUrl())
			.messageType("IMAGE")
			.content(imageMessage)
			.media(imageUrls)
			.mention(new ArrayList<>())
			.build();

		chatMessageRepository.save(chatMessage);

		ChatMessageDto chatMessageDto = ChatMessageDto.of(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		kafkaProducerService.send(chatMessageDto);

		List<String> nicknames = getChatRoomNickname(chatRoomId); // 방 참여자 목록
		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {
				boolean isActive = sessionService.isSessionExist(nickname);
				log.info(String.valueOf(isActive));
				if (!isActive) {
					chatRoomStatusService.incrementUnreadCount(chatRoomId.toString(), nickname);
					messageStatusService.setFirstUnreadMessageId(chatRoomId.toString(), nickname, chatMessage.getId());
				}
				chatRoomStatusService.addRecentUsers(chatMessageDto.getSender(), nickname, System.currentTimeMillis());
				chatRoomStatusService.addRecentUsers(nickname, chatMessageDto.getSender(), System.currentTimeMillis());
			}
		});

		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {

				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoomId.toString(),
					nickname);
				ChatUpdateDto chatUpdateDto = new ChatUpdateDto(chatRoomId, unreadCount, chatMessageDto.getContent(),
					LocalDateTime.now(), chatMessageDto.getSender(), nickname);

				kafkaProducerService.sendChatUpdate(chatUpdateDto);

			}
		});

	}

	@Transactional
	public void sendVideoMessage(UUID chatRoomId, String sender, MultipartFile video) {

		String videoMessage = "동영상을 보냈습니다";

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		FileRequestDto fileRequestDto = s3Service.saveFile(video, false);

		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoomId(chatRoomId.toString())
			.sender(sender)
			.profileUrl(user.getThumbnailUrl())
			.messageType("VIDEO")
			.content(videoMessage)
			.media(List.of(fileRequestDto.getFileUrl()))
			.mention(new ArrayList<>())
			.build();

		chatMessageRepository.save(chatMessage);
		ChatMessageDto chatMessageDto = ChatMessageDto.of(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		kafkaProducerService.send(chatMessageDto);

		List<String> nicknames = getChatRoomNickname(chatRoomId); // 방 참여자 목록
		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {
				boolean isActive = sessionService.isSessionExist(nickname);
				log.info(String.valueOf(isActive));
				if (!isActive) {
					chatRoomStatusService.incrementUnreadCount(chatRoomId.toString(), nickname);
					messageStatusService.setFirstUnreadMessageId(chatRoomId.toString(), nickname, chatMessage.getId());
				}
				chatRoomStatusService.addRecentUsers(chatMessageDto.getSender(), nickname, System.currentTimeMillis());
				chatRoomStatusService.addRecentUsers(nickname, chatMessageDto.getSender(), System.currentTimeMillis());
			}
		});

		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {

				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoomId.toString(),
					nickname);
				ChatUpdateDto chatUpdateDto = new ChatUpdateDto(chatRoomId, unreadCount, chatMessageDto.getContent(),
					LocalDateTime.now(), chatMessageDto.getSender(), nickname);

				kafkaProducerService.sendChatUpdate(chatUpdateDto);

			}
		});

	}

	@Transactional
	public void sendFileMessage(UUID chatRoomId, String sender, MultipartFile file) {
		String fileMessage = "파일을 보냈습니다";

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		FileRequestDto fileRequestDto = s3Service.saveFile(file, true);
		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoomId(chatRoomId.toString())
			.sender(sender)
			.profileUrl(user.getThumbnailUrl())
			.messageType("FILE")
			.content(fileMessage)
			.media(List.of(fileRequestDto.getFileUrl()))
			.mention(new ArrayList<>())
			.build();

		chatMessageRepository.save(chatMessage);
		ChatMessageDto chatMessageDto = ChatMessageDto.of(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		kafkaProducerService.send(chatMessageDto);

		List<String> nicknames = getChatRoomNickname(chatRoomId); // 방 참여자 목록
		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {
				boolean isActive = sessionService.isSessionExist(nickname);
				log.info(String.valueOf(isActive));
				if (!isActive) {
					chatRoomStatusService.incrementUnreadCount(chatRoomId.toString(), nickname);
					messageStatusService.setFirstUnreadMessageId(chatRoomId.toString(), nickname, chatMessage.getId());
				}
				chatRoomStatusService.addRecentUsers(chatMessageDto.getSender(), nickname, System.currentTimeMillis());
				chatRoomStatusService.addRecentUsers(nickname, chatMessageDto.getSender(), System.currentTimeMillis());
			}
		});

		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {

				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoomId.toString(),
					nickname);
				ChatUpdateDto chatUpdateDto = new ChatUpdateDto(chatRoomId, unreadCount, chatMessageDto.getContent(),
					LocalDateTime.now(), chatMessageDto.getSender(), nickname);

				kafkaProducerService.sendChatUpdate(chatUpdateDto);

			}
		});
	}

	@Transactional
	public void leaveChatRoom(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		chatRoomUser.setIsExited(true);
		// 단체 채팅방만
		if (!chatRoom.isPersonal()) {
			// 방 이름 업데이트
			List<ChatRoomUser> remainingUsers = chatRoomUserRepository.findAllByChatRoomIdAndIsExitedFalse(chatRoomId);

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
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUser.updatePinning(true);
	}

	@Transactional
	public void unpinByChatRoomUser(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(chatRoomId,
				nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUser.updatePinning(false);
	}

	@Transactional
	public void renameChatRoom(UUID chatRoomId, String nickname, RenameChatRoomRequestDto request) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNicknameAndIsExitedIsFalse(chatRoomId,
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

	public List<String> getChatRoomNickname(UUID chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다.", ErrorCode.NOT_FOUND
			));
		List<ChatRoomUser> chatRoomUsers;
		if (chatRoom.isPersonal()) {
			chatRoomUsers = chatRoomUserRepository.findAllByChatRoomId(chatRoomId);
			chatRoomUsers.forEach(user -> user.setIsExited(false));
		} else {
			chatRoomUsers = chatRoomUserRepository.findAllByChatRoomIdAndIsExitedFalse(chatRoomId);
		}
		return chatRoomUsers.stream()
			.map(chatRoomUser -> chatRoomUser.getUser().getNickname())
			.collect(Collectors.toList());
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