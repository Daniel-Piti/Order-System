package com.pt.ordersystem.ordersystem.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.secrets")
data class ApplicationSecrets(
  val jwt: String = "",
  val adminUsernameHash: String = "",
  val adminPasswordHash: String = ""
)
