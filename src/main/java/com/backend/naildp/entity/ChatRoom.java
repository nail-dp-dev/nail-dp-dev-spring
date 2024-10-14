package com.backend.naildp.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	@Column
	private LocalDateTime modifiedAt;

	public void updateLastMessage(List<String> content, LocalDateTime modifiedAt) {
		this.lastMessage = content.get(0);
		this.modifiedAt = modifiedAt;
	}

	public void updateParticipantCnt(int count) {
		this.participantCnt = count;
	}
}
