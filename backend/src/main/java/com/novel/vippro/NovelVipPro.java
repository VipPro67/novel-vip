package com.novel.vippro;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NovelVipPro {

	public static Dotenv dotenv = Dotenv.configure().load();

	public static void main(String[] args) {
		SpringApplication.run(NovelVipPro.class, args);
	}
}
