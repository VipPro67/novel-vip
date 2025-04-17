package com.novel.vippro;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NovelVipPro {

	public static void main(String[] args) {
		// backend/.env
		Dotenv.configure()
				.directory("backend")
				.load();
		SpringApplication.run(NovelVipPro.class, args);
	}
}
