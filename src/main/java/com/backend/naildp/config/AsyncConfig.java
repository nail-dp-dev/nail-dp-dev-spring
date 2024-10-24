package com.backend.naildp.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(100);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("async-");
		return executor;
	}

	@Bean
	public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor delegate) {
		return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
	}

	// @Bean
	// public ThreadPoolTaskExecutor taskExecutor() {
	// 	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	// 	executor.setCorePoolSize(5); // 기본 스레드 수
	// 	executor.setMaxPoolSize(10); // 최대 스레드 수
	// 	executor.setQueueCapacity(25); // 큐의 크기
	// 	executor.setThreadNamePrefix("async-"); // 스레드 이름 접두사
	// 	executor.initialize(); // 초기화
	// 	return executor;
	// }
	// @Bean
	// public DelegatingSecurityContextExecutorService delegatingSecurityContextExecutorService(ThreadPoolTaskExecutor taskExecutor) {
	// 	return new DelegatingSecurityContextExecutorService(taskExecutor.getThreadPoolExecutor());
	// }
}