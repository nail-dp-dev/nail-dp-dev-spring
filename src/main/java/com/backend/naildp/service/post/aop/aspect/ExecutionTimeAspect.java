package com.backend.naildp.service.post.aop.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

	@Around("execution(* com.backend.naildp.repository..*.*(..))")
	public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			long startTime = System.currentTimeMillis();
			log.info("[around][트랜잭션 시작] {}", joinPoint.getSignature());

			Object result = joinPoint.proceed();

			long endTime = System.currentTimeMillis();

			String method = joinPoint.getSignature().toShortString();
			Object[] args = joinPoint.getArgs();
			log.info("[around] Call: {} with args {} took {}ms", method, Arrays.toString(args), endTime - startTime);
			log.info("[around][트랜잭션 커밋] {}", joinPoint.getSignature());

			return result;
		} catch (Exception e) {
			log.info("[around][트랜잭션 롤백] {}", joinPoint.getSignature());
			throw e;
		} finally {
			log.info("[around][리소스 릴리즈] {}", joinPoint.getSignature());
		}
	}
}
