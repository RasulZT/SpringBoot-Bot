package com.parqour.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Configuration
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
public class TelegramBotApplication {

	@GetMapping("/")
	String home() {
		return "Spring is here!";
	}

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		SpringApplication.run(TelegramBotApplication.class, args);
	}
}