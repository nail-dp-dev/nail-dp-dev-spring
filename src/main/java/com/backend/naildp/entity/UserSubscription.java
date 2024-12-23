package com.backend.naildp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.martijndwars.webpush.Subscription;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_subscription_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private String endpoint;
	private String p256dh;
	private String auth;

	@Builder
	private UserSubscription(User user, String endpoint, String p256dh, String auth) {
		this.user = user;
		this.endpoint = endpoint;
		this.p256dh = p256dh;
		this.auth = auth;
	}

	public static UserSubscription from(User user, String endpoint, String p256dh, String auth) {
		return UserSubscription.builder()
			.user(user)
			.endpoint(endpoint)
			.p256dh(p256dh)
			.auth(auth)
			.build();
	}

	public boolean hasDifferentSubscriptionInfo(String endpoint) {
		return !this.endpoint.equals(endpoint);
	}

	public void updateInfo(String endpoint, String p256dh, String auth) {
		this.endpoint = endpoint;
		this.p256dh = p256dh;
		this.auth = auth;
	}
}
