package com.backend.naildp.service.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.RoomType;
import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.ChatUpdateDto;
import com.backend.naildp.dto.chat.MessageResponseDto;
import com.backend.naildp.dto.chat.MessageSummaryResponse;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.ChatRoom;
import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.mongo.ChatMessage;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
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
public class MessageService {
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
	public void sendMessage(ChatMessageDto chatMessageDto, UUID chatRoomId) {
		ChatRoom chatRoom;
		User user = userRepository.findByNickname(chatMessageDto.getSender())
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		log.info("sendMessage:chatRoomId:{}", chatRoomId);
		ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);

		if (chatRoomRequestDto != null) {
			log.info("check~@########");
			chatRoom = new ChatRoom();
			chatRoomRepository.save(chatRoom);

			int participantCnt = chatRoomRequestDto.getNickname().size();
			if (participantCnt == 2) {
				chatRoom.updateRoomType(RoomType.PERSONAL);
			} else {
				chatRoom.updateRoomType(RoomType.GROUP);
			}
			chatRoom.updateParticipantCnt(participantCnt);

			chatRoomRequestDto.getNickname().forEach(participant -> {
				User findUser = userRepository.findByNickname(participant)
					.orElseThrow(() -> new CustomException("해당 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

				String roomName = chatRoomRequestDto.getNickname().stream()
					.filter(nickname -> !nickname.equals(participant))
					.collect(Collectors.joining(", "));

				ChatRoomUser chatRoomUser = new ChatRoomUser(findUser, chatRoom);
				chatRoomUserRepository.save(chatRoomUser);
				chatRoomUser.updateRoomName(roomName);
			});

			chatRoomStatusService.deleteTempChatRoom(chatRoomId);
		} else {
			chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		}

		ChatMessage chatMessage = new ChatMessage(chatMessageDto, chatRoom.getId(), user);
		chatMessageRepository.save(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		chatMessageDto.setChatRoomId(chatRoom.getId().toString());

		kafkaProducerService.send(chatMessageDto);

		List<String> nicknames = getChatRoomNickname(chatRoom.getId()); // 방 참여자 목록
		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {
				boolean isActive = sessionService.isSessionExist(nickname);
				log.info(String.valueOf(isActive));
				if (!isActive) {
					chatRoomStatusService.incrementUnreadCount(chatRoom.getId().toString(), nickname);
					messageStatusService.setFirstUnreadMessageId(chatRoom.getId().toString(), nickname,
						chatMessage.getId());
				}
				chatRoomStatusService.addRecentUsers(chatMessageDto.getSender(), nickname, System.currentTimeMillis());
				chatRoomStatusService.addRecentUsers(nickname, chatMessageDto.getSender(), System.currentTimeMillis());

			}
		});

		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {

				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoom.getId().toString(),
					nickname);
				ChatUpdateDto chatUpdateDto = new ChatUpdateDto(chatRoom.getId(), unreadCount,
					chatMessageDto.getContent(),
					LocalDateTime.now(), chatMessageDto.getSender(), nickname);

				kafkaProducerService.sendChatUpdate(chatUpdateDto);

			}
		});

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

	@Transactional(readOnly = true)
	public MessageSummaryResponse getMessagesByRoomId(UUID chatRoomId, String nickname) {
		List<MessageSummaryResponse.ChatUserInfoResponse> chatUserInfo;

		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(chatRoomId, nickname)
			.orElseThrow(() -> new CustomException("해당 방을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		String firstUnreadMessageId = messageStatusService.getFirstUnreadMessageId(chatRoomId.toString(), nickname);

		List<ChatMessage> messages;

		LocalDateTime rejoinedAt = chatRoomUser.getRejoinedAt();

		if (rejoinedAt == null) {
			messages = chatMessageRepository.findAllByChatRoomId(chatRoomId.toString());
		} else {
			messages = chatMessageRepository.findAllByChatRoomIdAndCreatedAtAfter(chatRoomId.toString(), rejoinedAt);
		}
		List<MessageResponseDto> messageDto = messages.stream().map(message -> {
			Long unreadUserCount = messageStatusService.getUnreadUserCount(chatRoomId.toString(), message.getId());
			return MessageResponseDto.of(message, unreadUserCount);
		}).collect(Collectors.toList());

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
		if (chatRoom == null) {
			ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);
			chatRoomRequestDto.getNickname().remove(nickname);
			chatUserInfo = chatRoomRequestDto.getNickname().stream().map(name -> {
				User user = userRepository.findByNickname(name)
					.orElseThrow(() -> new CustomException("해당 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
				boolean isActive = sessionService.isSessionExist(user.getNickname());
				return new MessageSummaryResponse.ChatUserInfoResponse(user.getNickname(), user.getThumbnailUrl(),
					isActive,
					false);
			}).toList();
			return new MessageSummaryResponse(messageDto, firstUnreadMessageId, chatUserInfo);
		}

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

	public List<String> getChatRoomNickname(UUID chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다.", ErrorCode.NOT_FOUND
			));
		List<ChatRoomUser> chatRoomUsers;
		if (chatRoom.isPersonal()) {
			chatRoomUsers = chatRoomUserRepository.findAllByChatRoomId(chatRoomId);
			chatRoomUsers.forEach(user -> user.setRejoinedAt(LocalDateTime.now()));
		} else {
			chatRoomUsers = chatRoomUserRepository.findAllByChatRoomIdAndIsExitedFalse(chatRoomId);
		}
		return chatRoomUsers.stream()
			.map(chatRoomUser -> chatRoomUser.getUser().getNickname())
			.collect(Collectors.toList());
	}
}