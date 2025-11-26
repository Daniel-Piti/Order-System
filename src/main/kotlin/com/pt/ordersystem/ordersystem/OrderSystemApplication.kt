package com.pt.ordersystem.ordersystem

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class OrderSystemApplication

fun main(args: Array<String>) {
	// Load .env file for local development (if it exists)
	// Reads .env file and sets System properties, which Spring Boot uses to resolve ${VAR_NAME} placeholders
	try {
		Dotenv.configure()
			.ignoreIfMissing()
			.directory("./")
			.load()
			.entries()
			.forEach { System.setProperty(it.key, it.value) }
	} catch (e: Exception) {
		// .env file doesn't exist (normal in production/ECS where env vars are set directly)
	}
	
	runApplication<OrderSystemApplication>(*args)
}
