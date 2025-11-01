package com.pt.ordersystem.ordersystem.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "config")
data class ApplicationConfig(
  val jwt: String = "",

  val adminUsernameHash: String = "",
  val adminPasswordHash: String = "",

  val r2: R2Properties = R2Properties(),

  val maxUploadFileSizeMb: Int = 5 // Default value 5
)

data class R2Properties(
  val bucketName: String = "",
  val region: String = "",
  val accountId: String = "",
  val accessKey: String = "",
  val secretKey: String = "",
  val publicDomain: String = ""
)
