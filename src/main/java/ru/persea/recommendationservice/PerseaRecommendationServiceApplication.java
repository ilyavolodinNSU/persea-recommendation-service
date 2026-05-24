package ru.persea.recommendationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PerseaRecommendationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PerseaRecommendationServiceApplication.class, args);
	}

}
