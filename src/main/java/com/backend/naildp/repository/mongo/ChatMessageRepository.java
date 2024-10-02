package com.backend.naildp.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.backend.naildp.entity.mongo.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
}
