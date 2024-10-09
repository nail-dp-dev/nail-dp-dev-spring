package com.backend.naildp.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.backend.naildp.entity.mongo.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
	List<ChatMessage> findAllByChatRoomId(UUID chatRoomId);
}
