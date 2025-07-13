package com.pt.ordersystem.ordersystem.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class AppConfig {

  @Bean
  fun passwordEncoder() = BCryptPasswordEncoder()
}