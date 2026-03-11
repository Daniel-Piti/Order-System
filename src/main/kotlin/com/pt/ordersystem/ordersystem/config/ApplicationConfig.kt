package com.pt.ordersystem.ordersystem.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "config")
data class ApplicationConfig(
  val jwt: String = "",

  val adminUsernameHash: String = "",
  val adminPasswordHash: String = "",

  val s3: S3Properties = S3Properties(),

  val invoiceSigning: InvoiceSigningProperties = InvoiceSigningProperties()
)

data class InvoiceSigningProperties(
  /** Base64-encoded PKCS12 keystore (e.g. from AWS Secrets Manager or .env). */
  val keystoreBase64: String = "",
  val keystorePassword: String = "",
  val keyAlias: String = "",
  val keyPassword: String = ""
)

data class S3Properties(
  val bucketName: String = "",
  val region: String = "",
  val publicDomain: String = "" // CloudFront's domain
)
