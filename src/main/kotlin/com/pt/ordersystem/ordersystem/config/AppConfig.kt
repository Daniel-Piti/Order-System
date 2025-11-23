package com.pt.ordersystem.ordersystem.config

import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.crypto.SecretKey

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ApplicationConfig::class)
class AppConfig {

  @Bean
  fun passwordEncoder() = BCryptPasswordEncoder()

  @Bean
  fun jwtSigningKey(config: ApplicationConfig): SecretKey {
    require(config.jwt.isNotBlank()) { "config.jwt is not set" }
    require(config.jwt.length >= 32) { "config.jwt must be at least 32 characters for HS256" }
    return Keys.hmacShaKeyFor(config.jwt.toByteArray())
  }
}