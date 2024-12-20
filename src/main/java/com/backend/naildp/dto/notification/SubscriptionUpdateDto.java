package com.backend.naildp.dto.notification;

import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UserSubscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpdateDto {

	private String endpoint;
	private String auth;
	private String p256dh;

	public UserSubscription toEntity(User user) {
		return UserSubscription.from(user, this.endpoint, this.p256dh, this.auth);
	}

}
