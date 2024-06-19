package com.backend.naildp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NaildpApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaildpApplication.class, args);
	}

}
