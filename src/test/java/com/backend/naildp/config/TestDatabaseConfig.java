package com.backend.naildp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import jakarta.annotation.PreDestroy;

@TestConfiguration
public class TestDatabaseConfig {

	@Container
	private static final MySQLContainer<?> mysqlContainer;

	static {
		mysqlContainer = new MySQLContainer<>("mysql:8.0.33")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");
		mysqlContainer.start();

		String originalJdbcUrl = mysqlContainer.getJdbcUrl() + "?rewriteBatchedStatements=true";

		System.setProperty("spring.datasource.url", originalJdbcUrl);
		System.setProperty("spring.datasource.username", mysqlContainer.getUsername());
		System.setProperty("spring.datasource.password", mysqlContainer.getPassword());
	}

	@Bean
	public MySQLContainer<?> mysqlContainer() {
		return mysqlContainer;
	}

	@PreDestroy
	public void destroy() {
		if(mysqlContainer != null && mysqlContainer.isRunning()) {
			mysqlContainer.stop();
		}
	}
}
