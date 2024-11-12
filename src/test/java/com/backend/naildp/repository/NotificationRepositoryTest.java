package com.backend.naildp.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.User;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@Import({JpaAuditingConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

	@Autowired
	EntityManager em;

	@Autowired
	NotificationRepository notificationRepository;

	@Test
	void findNotifications() {
		//given
		User receiver = createUserByNickname("receiver");
		User sender = createUserByNickname("sender");
		List<Notification> notifications = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Notification notification = createNotification(receiver, sender);
			notifications.add(notification);
		}
		em.flush();
		em.clear();

		//when
		List<Notification> findNotifications = notificationRepository.findNotificationsByIdInAndReceiverNickname(
			notifications.stream().map(Notification::getId).toList(),
			receiver.getNickname());

		//then
		assertThat(findNotifications).hasSize(5);
		assertThat(findNotifications).extracting(Notification::getReceiver)
			.extracting(User::getNickname)
			.containsOnly(receiver.getNickname());
		assertThat(findNotifications).extracting(Notification::getSender)
			.extracting(User::getNickname)
			.containsOnly(sender.getNickname());
	}

	@Test
	void readNotifications() {
		//given
		User receiver = createUserByNickname("receiver");
		User sender = createUserByNickname("sender");
		for (int i = 0; i < 5; i++) {
			Notification notification = createNotification(receiver, sender);
		}
		em.flush();
		em.clear();


		//when
		List<Notification> notifications = findNotificationByReceiver(receiver);
		int readCount = notificationRepository.changeReadStatus(notifications);

		//then
		List<Notification> readNotifications = findNotificationByReceiver(receiver);
		assertThat(readNotifications).extracting(Notification::isRead).containsOnly(true);
		assertThat(readCount).isEqualTo(5);
	}

	private List<Notification> findNotificationByReceiver(User receiver) {
		return em.createQuery(
				"select n from Notification n where n.receiver.nickname = :nickname", Notification.class)
			.setParameter("nickname", receiver.getNickname())
			.getResultList();
	}

	private User createUserByNickname(String receiver) {
		User user = User.builder()
			.nickname(receiver)
			.phoneNumber("")
			.agreement(true)
			.role(UserRole.USER)
			.thumbnailUrl("")
			.build();
		em.persist(user);
		return user;
	}

	private Notification createNotification(User receiver, User sender) {
		Notification notification = Notification.builder()
			.receiver(receiver)
			.sender(sender)
			.link("")
			.notificationType(NotificationType.FOLLOW)
			.content("")
			.isRead(false)
			.build();
		return notificationRepository.save(notification);
	}

}