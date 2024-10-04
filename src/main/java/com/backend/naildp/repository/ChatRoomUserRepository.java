package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.ChatRoomUser;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
}
