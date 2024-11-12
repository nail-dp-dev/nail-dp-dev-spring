package com.backend.naildp.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.config.QueryDslTestConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfiguration.class, QueryDslTestConfig.class})
class NotificationRepositoryImplTest {

	@Autowired
	JPAQueryFactory queryFactory;

	@PersistenceContext
	EntityManager em;


}