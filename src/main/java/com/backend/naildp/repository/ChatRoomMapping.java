package com.backend.naildp.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ChatRoomMapping {
	UUID getId();

	String getName();

	String getLastMessage();

	Integer getParticipantCnt();

	LocalDateTime getModifiedAt();
}
