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
import com.backend.naildp.dto.chat.TempRoomSwitchDto;
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
	public UUID sendMessage(ChatMessageDto chatMessageDto, UUID chatRoomId) {
		ChatRoom chatRoom;
		User user = userRepository.findByNickname(chatMessageDto.getSender())
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		log.info("sendMessage:chatRoomId:{}", chatRoomId);

		// 임시 채팅방 가져오기(메시지를 한번도 보낸적 없는 채팅방)
		ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);

		if (chatRoomRequestDto != null) {
			// 임시채팅방 존재 -> 실제 채팅방으로 생성
			chatRoom = createChatRoom(chatRoomRequestDto);
			chatRoomId = chatRoom.getId();

			chatRoomStatusService.deleteTempChatRoom(chatRoomId);
			TempRoomSwitchDto tempRoomSwitchDto = new TempRoomSwitchDto(chatMessageDto.getSender(), chatRoomId);
			kafkaProducerService.sendChatRoomSwitchEvent(tempRoomSwitchDto);
		} else {
			// 기존 채팅방 그대로 사용
			chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		}

		ChatMessage chatMessage = new ChatMessage(chatMessageDto, chatRoom.getId(), user);
		chatMessageRepository.save(chatMessage);
		chatRoom.updateLastMessage(chatMessageDto.getContent());

		chatMessageDto.setChatRoomId(chatRoom.getId().toString());

		// 메시지 전송
		kafkaProducerService.send(chatMessageDto);
		// 채팅방 참여자들에게 채팅방 정보 실시간 업데이트
		updateChatRoomForParticipants(chatRoomId, chatMessageDto, chatMessage);
		return chatRoomId;
	}

	@Transactional
	public void sendImageMessages(UUID chatRoomId, String sender, List<MultipartFile> imageFiles) {
		String imageMessage = "사진을 보냈습니다";

		ChatRoom chatRoom;
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		// 임시 채팅방 가져오기(메시지를 한번도 보낸적 없는 채팅방)
		ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);

		if (chatRoomRequestDto != null) {
			// 임시채팅방 존재 -> 실제 채팅방으로 생성
			chatRoom = createChatRoom(chatRoomRequestDto);
			chatRoomId = chatRoom.getId();

			chatRoomStatusService.deleteTempChatRoom(chatRoomId);
			TempRoomSwitchDto tempRoomSwitchDto = new TempRoomSwitchDto(sender, chatRoomId);
			kafkaProducerService.sendChatRoomSwitchEvent(tempRoomSwitchDto);
		} else {
			// 기존 채팅방 그대로 사용
			chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		}

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

		updateChatRoomForParticipants(chatRoomId, chatMessageDto, chatMessage);

	}

	@Transactional
	public void sendVideoMessage(UUID chatRoomId, String sender, MultipartFile video) {

		String videoMessage = "동영상을 보냈습니다";

		ChatRoom chatRoom;
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		// 임시 채팅방 가져오기(메시지를 한번도 보낸적 없는 채팅방)
		ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);

		if (chatRoomRequestDto != null) {
			// 임시채팅방 존재 -> 실제 채팅방으로 생성
			chatRoom = createChatRoom(chatRoomRequestDto);
			chatRoomId = chatRoom.getId();

			chatRoomStatusService.deleteTempChatRoom(chatRoomId);
			TempRoomSwitchDto tempRoomSwitchDto = new TempRoomSwitchDto(sender, chatRoomId);
			kafkaProducerService.sendChatRoomSwitchEvent(tempRoomSwitchDto);
		} else {
			// 기존 채팅방 그대로 사용
			chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		}

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

		updateChatRoomForParticipants(chatRoomId, chatMessageDto, chatMessage);

	}

	@Transactional
	public void sendFileMessage(UUID chatRoomId, String sender, MultipartFile file) {
		String fileMessage = "파일을 보냈습니다";

		ChatRoom chatRoom;
		User user = userRepository.findByNickname(sender)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		// 임시 채팅방 가져오기(메시지를 한번도 보낸적 없는 채팅방)
		ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);

		if (chatRoomRequestDto != null) {
			// 임시채팅방 존재 -> 실제 채팅방으로 생성
			chatRoom = createChatRoom(chatRoomRequestDto);
			chatRoomId = chatRoom.getId();

			chatRoomStatusService.deleteTempChatRoom(chatRoomId);
			TempRoomSwitchDto tempRoomSwitchDto = new TempRoomSwitchDto(sender, chatRoomId);
			kafkaProducerService.sendChatRoomSwitchEvent(tempRoomSwitchDto);
		} else {
			// 기존 채팅방 그대로 사용
			chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다", ErrorCode.NOT_FOUND));
		}

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

		updateChatRoomForParticipants(chatRoomId, chatMessageDto, chatMessage);

	}

	// 특정 채팅방 내 메시지 조회
	@Transactional(readOnly = true)
	public MessageSummaryResponse getMessagesByRoomId(UUID chatRoomId, String nickname) {
		ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserNickname(chatRoomId, nickname)
			.orElse(null);
		LocalDateTime rejoinedAt = (chatRoomUser != null) ? chatRoomUser.getRejoinedAt() : null;

		// 메시지 조회
		List<ChatMessage> messages = getChatMessages(chatRoomId, rejoinedAt);

		List<MessageResponseDto> messageDto = messages.stream().map(message -> {
			Long unreadUserCount = messageStatusService.getUnreadUserCount(chatRoomId.toString(), message.getId());
			return MessageResponseDto.of(message, unreadUserCount);
		}).collect(Collectors.toList());

		String firstUnreadMessageId = messageStatusService.getFirstUnreadMessageId(chatRoomId.toString(), nickname);

		// 채팅방과 유저 정보 조회
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
		List<MessageSummaryResponse.ChatUserInfoResponse> chatUserInfo = (chatRoom == null)
			? getTempChatUserInfo(chatRoomId, nickname)
			: getActiveChatUserInfo(chatRoomId, nickname);

		return new MessageSummaryResponse(messageDto, firstUnreadMessageId, chatUserInfo);
	}

	// 최초로 메시지 보냈을 때, 실제로 생성되는 채팅방
	private ChatRoom createChatRoom(ChatRoomRequestDto chatRoomRequestDto) {
		ChatRoom chatRoom = new ChatRoom();
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

			String roomName = chatRoomRequestDto.getNickname()
				.stream()
				.filter(nickname -> !nickname.equals(participant))
				.collect(Collectors.joining(", "));

			ChatRoomUser chatRoomUser = new ChatRoomUser(findUser, chatRoom);
			chatRoomUserRepository.save(chatRoomUser);
			chatRoomUser.updateRoomName(roomName);
		});
		return chatRoom;
	}

	// 모든 참가자에 대한 채팅방 정보 업데이트
	private void updateChatRoomForParticipants(UUID chatRoomId, ChatMessageDto chatMessageDto,
		ChatMessage chatMessage) {
		List<String> participants = getNicknamesByChatRoom(chatRoomId);

		participants.forEach(participant -> {
			if (!participant.equals(chatMessageDto.getSender())) {
				// 채팅방에 들어와 있는지 확인
				boolean isActive = sessionService.isSessionExist(participant);
				if (!isActive) {
					chatRoomStatusService.incrementUnreadCount(chatRoomId.toString(), participant);
					messageStatusService.setFirstUnreadMessageId(chatRoomId.toString(), participant,
						chatMessage.getId());
				}
				// 최근 대화 상대 저장
				chatRoomStatusService.addRecentUsers(chatMessageDto.getSender(), participant,
					System.currentTimeMillis());
				chatRoomStatusService.addRecentUsers(participant, chatMessageDto.getSender(),
					System.currentTimeMillis());

				// 안읽은 메시지 업데이트
				int unreadCount = chatRoomStatusService.getUnreadCount(chatRoomId.toString(), participant);
				ChatUpdateDto chatUpdateDto = new ChatUpdateDto(chatRoomId, unreadCount,
					chatMessageDto.getContent(), LocalDateTime.now(), chatMessageDto.getSender(), participant);

				kafkaProducerService.sendChatUpdate(chatUpdateDto);
			}
		});
	}

	// 특정 채팅방 참여자 리스트 가져오기
	private List<String> getNicknamesByChatRoom(UUID chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		List<ChatRoomUser> chatRoomUsers;
		if (chatRoom.isPersonal()) {
			// 개인 채팅이라면 메시지 보냈을때, 상대 유저 재입장
			chatRoomUsers = chatRoomUserRepository.findAllByChatRoomId(chatRoomId);
			chatRoomUsers.forEach(user -> {
				if (user.getIsExited()) {
					user.setRejoinedAt(LocalDateTime.now());
				}
			});
		} else {
			chatRoomUsers = chatRoomUserRepository.findAllByChatRoomIdAndIsExitedFalse(chatRoomId);
		}
		return chatRoomUsers.stream()
			.map(chatRoomUser -> chatRoomUser.getUser().getNickname())
			.collect(Collectors.toList());
	}

	// 재입장 시점을 기준으로 메시지 조회
	private List<ChatMessage> getChatMessages(UUID chatRoomId, LocalDateTime rejoinedAt) {
		if (rejoinedAt == null) {
			return chatMessageRepository.findAllByChatRoomId(chatRoomId.toString());
		}
		return chatMessageRepository.findAllByChatRoomIdAndCreatedAtAfter(chatRoomId.toString(), rejoinedAt);
	}

	// 임시 채팅방 유저 정보 조회
	private List<MessageSummaryResponse.ChatUserInfoResponse> getTempChatUserInfo(UUID chatRoomId, String nickname) {
		ChatRoomRequestDto chatRoomRequestDto = chatRoomStatusService.getTempChatRoom(chatRoomId);
		return chatRoomRequestDto.getNickname().stream()
			.filter(name -> !name.equals(nickname))
			.map(name -> {
				User user = userRepository.findByNickname(name)
					.orElseThrow(() -> new CustomException("해당 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
				return createChatUserInfo(user);
			})
			.collect(Collectors.toList());
	}

	// 실제 채팅방 유저 정보 조회
	private List<MessageSummaryResponse.ChatUserInfoResponse> getActiveChatUserInfo(UUID chatRoomId, String nickname) {
		List<User> roomUsers = userRepository.findAllByChatRoomIdNotInMyNickname(chatRoomId, nickname);
		if (roomUsers.isEmpty()) {
			User user = userRepository.findByNickname(nickname)
				.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", ErrorCode.NOT_FOUND));
			return List.of(createChatUserInfo(user));
		}
		return roomUsers.stream().map(this::createChatUserInfo).collect(Collectors.toList());
	}

	// ChatUserInfoResponse 객체 생성
	private MessageSummaryResponse.ChatUserInfoResponse createChatUserInfo(User user) {
		boolean isActive = sessionService.isSessionExist(user.getNickname());
		return new MessageSummaryResponse.ChatUserInfoResponse(user.getNickname(), user.getThumbnailUrl(), isActive,
			false);
	}

}