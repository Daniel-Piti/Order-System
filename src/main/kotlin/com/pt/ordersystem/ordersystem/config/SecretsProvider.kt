package com.pt.ordersystem.ordersystem.config

import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class SecretsProvider(
  private val secrets: ApplicationSecrets
) {
  
  val jwtSigningKey: SecretKey = run {
    require(secrets.jwt.isNotBlank()) { "app.secrets.jwt is not set" }
    require(secrets.jwt.length >= 32) { "app.secrets.jwt must be at least 32 characters for HS256" }
    Keys.hmacShaKeyFor(secrets.jwt.toByteArray())
  }

  val adminUsernameHash: String = secrets.adminUsernameHash

  val adminPasswordHash: String = secrets.adminPasswordHash
}

