package com.pt.ordersystem.ordersystem.config

import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class ConfigProvider(
  private val config: ApplicationConfig
) {
  
  val jwtSigningKey: SecretKey = run {
    require(config.jwt.isNotBlank()) { "config.jwt is not set" }
    require(config.jwt.length >= 32) { "config.jwt must be at least 32 characters for HS256" }
    Keys.hmacShaKeyFor(config.jwt.toByteArray())
  }

  val adminUsernameHash: String = config.adminUsernameHash

  val adminPasswordHash: String = config.adminPasswordHash
  
  // R2 configuration access
  val r2: R2Properties = config.r2
}

