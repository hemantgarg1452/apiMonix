package com.apimonix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApimonixApplication {

	public static void main(String[] args) {
		//SpringApplication.run(ApimonixApplication.class, args);
		SpringApplication app = new SpringApplication(ApimonixApplication.class);
		app.run(args);
		System.out.println("DB URL: " + System.getenv("SUPABASE_DB_URL"));
	}

}
