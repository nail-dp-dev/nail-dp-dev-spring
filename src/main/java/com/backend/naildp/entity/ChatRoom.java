package com.backend.naildp.entity;

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
}
