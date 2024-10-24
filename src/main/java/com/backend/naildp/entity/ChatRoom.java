package com.backend.naildp.entity;

import java.util.UUID;

import com.backend.naildp.common.RoomType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "chat_room_id")
	private UUID id;

	@Column
	private String lastMessage;

	@Column
	private int participantCnt;

	@Enumerated(EnumType.STRING)
	private RoomType roomType;

	public void updateLastMessage(String content) {
		this.lastMessage = content;
	}

	public void updateParticipantCnt(int count) {
		this.participantCnt = count;
	}

	public void updateRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public boolean isPersonal() {
		return this.roomType == RoomType.PERSONAL;
	}

}
