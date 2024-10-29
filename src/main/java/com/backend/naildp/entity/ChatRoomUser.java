package com.backend.naildp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomUser extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_room_user_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@Column
	private String name = "default";

	private Boolean isExited = false;

	private Boolean isPinning = false;

	public ChatRoomUser(User user, ChatRoom chatRoom) {
		this.user = user;
		this.chatRoom = chatRoom;
	}

	public void updateRoomName(String roomName) {
		this.name = roomName;
	}

	public void updatePinning(boolean isPinning) {
		this.isPinning = isPinning;
	}

	public void setIsExited(boolean isExited) {
		this.isExited = isExited;
	}
}
