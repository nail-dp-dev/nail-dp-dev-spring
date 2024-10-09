package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.ChatRoomUser;
import com.backend.naildp.entity.User;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
	List<ChatRoomUser> findAllByUser(User user);
}
