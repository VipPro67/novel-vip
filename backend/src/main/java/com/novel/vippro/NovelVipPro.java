package com.novel.vippro;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NovelVipPro {

	public static Dotenv dotenv = Dotenv.configure().directory("./backend").load();

	public static void main(String[] args) {
		for (DotenvEntry entry : dotenv.entries()) {
			System.setProperty(entry.getKey(), entry.getValue());
		}
		SpringApplication.run(NovelVipPro.class, args);
	}
}
