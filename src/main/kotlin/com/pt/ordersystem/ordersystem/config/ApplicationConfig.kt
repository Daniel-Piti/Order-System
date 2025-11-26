package com.pt.ordersystem.ordersystem.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "config")
data class ApplicationConfig(
  val jwt: String = "",

  val adminUsernameHash: String = "",
  val adminPasswordHash: String = "",

  val s3: S3Properties = S3Properties()
)

data class S3Properties(
  val bucketName: String = "",
  val region: String = "",
  val publicDomain: String = "" // CloudFront's domain
)
