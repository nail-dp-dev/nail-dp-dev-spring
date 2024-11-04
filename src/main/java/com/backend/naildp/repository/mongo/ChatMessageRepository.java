package com.backend.naildp.repository.mongo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.backend.naildp.entity.mongo.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
	List<ChatMessage> findAllByChatRoomIdAndCreatedAtAfter(String chatRoomId, LocalDateTime rejoinedAt);

	List<ChatMessage> findAllByChatRoomId(String chatRoomId);

}
