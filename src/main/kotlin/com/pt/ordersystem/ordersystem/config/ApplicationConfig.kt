package com.pt.ordersystem.ordersystem.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "config")
data class ApplicationConfig(
  val jwt: String = "",
  val adminUsernameHash: String = "",
  val adminPasswordHash: String = "",
  
  // Cloudflare R2 configuration (all in one place)
  val r2: R2Properties = R2Properties()
)

data class R2Properties(
  val bucketName: String = "",
  val region: String = "",
  val accountId: String = "",
  val accessKey: String = "",
  val secretKey: String = ""
)
