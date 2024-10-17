package com.backend.naildp.service.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.dto.chat.ChatListResponse;
import com.backend.naildp.dto.chat.ChatListSummaryResponse;
import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.MessageResponseDto;
import com.backend.naildp.dto.chat.MessageSummaryResponse;
import com.backend.naildp.dto.chat.RenameChatRoomRequestDto;
import com.backend.naildp.dto.post.FileRequestDto;
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

	@Transactional
	public UUID createChatRoom(String myNickname, ChatRoomRequestDto chatRoomRequestDto) {
		// 1:1 채팅일 경우
		chatRoomRequestDto.getNickname().add(myNickname);
		int participantCnt = chatRoomRequestDto.getNickname().size();

		if (participantCnt == 1) {
			List<String> userNames = Arrays.asList(myNickname, chatRoomRequestDto.getNickname().get(0));
			Optional<ChatRoom> chatRoom = chatRoomRepository.findChatRoomByUsers(userNames, userNames.size());
			if (chatRoom.isPresent()) {
				return chatRoom.get().getId();
			}
		}
		ChatRoom chatRoom = new ChatRoom();
		chatRoomRepository.save(chatRoom);
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
	public String sendMessage(ChatMessageDto chatMessageDto, UUID chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		User user = userRepository.findByNickname(chatMessageDto.getSender())
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		ChatMessage chatMessage = new ChatMessage(chatMessageDto, chatRoomId, user);
		chatMessageRepository.save(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		chatMessageDto.setChatRoomId(chatRoomId.toString());

		return chatMessage.getId();
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
		String firstUnreadMessageId = messageStatusService.getFirstUnreadMessageId(chatRoomId.toString(), nickname);

		List<ChatMessage> messages = chatMessageRepository.findAllByChatRoomId(chatRoomId.toString());
		List<MessageResponseDto> messageDto = messages.stream().map(message -> {
			Long unreadUserCount = messageStatusService.getUnreadUserCount(chatRoomId.toString(), message.getId());
			return MessageResponseDto.of(message, unreadUserCount);
		}).collect(Collectors.toList());

		List<User> roomUsers = userRepository.findAllByChatRoomIdNotInMyNickname(chatRoomId, nickname);
		List<MessageSummaryResponse.ChatUserInfoResponse> chatUserInfo = roomUsers.stream().map(user -> {
			boolean isActive = sessionService.isSessionExist(user.getNickname());
			return new MessageSummaryResponse.ChatUserInfoResponse(user.getNickname(), user.getThumbnailUrl(), isActive,
				false);
		}).toList();

		return new MessageSummaryResponse(messageDto, firstUnreadMessageId, chatUserInfo);
	}

	@Transactional(readOnly = true)
	public List<String> getChatRoomNickname(UUID chatRoomId) {
		List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByChatRoomId(chatRoomId);

		return chatRoomUsers.stream()
			.map(chatRoomUser -> chatRoomUser.getUser().getNickname())
			.collect(Collectors.toList());
	}

	@Transactional
	public ChatMessageDto sendImageMessages(UUID chatRoomId, String sender, List<MultipartFile> imageFiles) {
		String imageMessage = "사진을 보냈습니다";

		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		List<FileRequestDto> fileRequestDtos = s3Service.saveFiles(imageFiles);
		List<String> imageUrls = fileRequestDtos.stream().map(FileRequestDto::getFileUrl).collect(Collectors.toList());

		return createAndSaveMessage(chatRoomId, sender, user.getThumbnailUrl(), "IMAGE", "사진을 보냈습니다", imageUrls);
	}

	@Transactional
	public ChatMessageDto sendVideoMessage(UUID chatRoomId, String sender, MultipartFile video) {

		String videoMessage = "동영상을 보냈습니다";
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		FileRequestDto fileRequestDto = s3Service.saveFile(video, false);

		return createAndSaveMessage(chatRoomId, sender, user.getThumbnailUrl(), "VIDEO", "동영상을 보냈습니다",
			List.of(fileRequestDto.getFileUrl()));
	}

	@Transactional
	public ChatMessageDto sendFileMessage(UUID chatRoomId, String sender, MultipartFile file) {

		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		FileRequestDto fileRequestDto = s3Service.saveFile(file, true);
		return createAndSaveMessage(chatRoomId, sender, user.getThumbnailUrl(), "FILE", "파일을 보냈습니다",
			List.of(fileRequestDto.getFileUrl()));
	}

	@Transactional
	public void leaveChatRoom(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(chatRoomId, nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUserRepository.delete(chatRoomUser);

		// 방 이름 업데이트
		List<ChatRoomUser> remainingUsers = chatRoomUserRepository.findAllByChatRoomId(chatRoomId);
		for (ChatRoomUser remainingUser : remainingUsers) {
			String updatedRoomName = remainingUsers.stream()
				.filter(user -> !user.getUser().getNickname().equals(remainingUser.getUser().getNickname()))
				.map(user -> user.getUser().getNickname())
				.collect(Collectors.joining(", "));

			remainingUser.updateRoomName(updatedRoomName); // 방 이름 업데이트
		}

		if (remainingUsers.isEmpty()) {
			chatRoomRepository.deleteById(chatRoomId);
		}
	}

	@Transactional
	public void pinByChatRoomUser(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(chatRoomId, nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUser.updatePinning(true);
	}

	public void unpinByChatRoomUser(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(chatRoomId, nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));
		chatRoomUser.updatePinning(false);
	}

	@Transactional
	public ChatMessageDto createAndSaveMessage(UUID chatRoomId, String sender, String profileUrl, String messageType,
		String content, List<String> mediaUrls) {
		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoomId(chatRoomId.toString())
			.sender(sender)
			.profileUrl(profileUrl)
			.messageType(messageType)
			.content(content)
			.media(mediaUrls)
			.mention(new ArrayList<>())
			.build();

		chatMessageRepository.save(chatMessage);
		return ChatMessageDto.of(chatMessage);
	}

	@Transactional
	public void renameChatRoom(UUID chatRoomId, String nickname, RenameChatRoomRequestDto request) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(chatRoomId, nickname)
			.orElseThrow(() -> new CustomException("해당 채팅방에 참여 중이지 않습니다.", ErrorCode.NOT_FOUND));

		chatRoomUser.updateRoomName(request.getChatRoomName());
	}
}
