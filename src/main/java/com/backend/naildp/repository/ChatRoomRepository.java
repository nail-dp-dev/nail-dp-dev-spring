package com.backend.naildp.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

}
