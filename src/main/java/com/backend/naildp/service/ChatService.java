package com.backend.naildp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatService {
	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public UUID createChatRoom(String myNickname, ChatRoomRequestDto chatRoomRequestDto) {
		ChatRoom chatRoom = new ChatRoom();
		chatRoomRepository.save(chatRoom);

		chatRoomRequestDto.getNickname().add(myNickname);

		chatRoomRequestDto.getNickname().forEach(nickname -> {
			User user = userRepository.findByNickname(nickname)
				.orElseThrow(() -> new CustomException("해당 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

			ChatRoomUser chatRoomUser = new ChatRoomUser(user, chatRoom);
			chatRoomUserRepository.save(chatRoomUser);
		});
		return chatRoom.getId();
	}

	public String sendMessage(ChatMessageDto chatMessageDto, UUID chatRoomId) {
		ChatMessage chatMessage = new ChatMessage(chatMessageDto, chatRoomId);
		chatMessageRepository.save(chatMessage);
		return chatMessage.getId();
	}
}
