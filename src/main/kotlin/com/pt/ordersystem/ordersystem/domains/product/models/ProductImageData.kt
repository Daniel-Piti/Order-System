package com.pt.ordersystem.ordersystem.domains.product.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pt.ordersystem.ordersystem.storage.S3Helper

private val objectMapper = jacksonObjectMapper()

private data class ProductImageRow(val s3Key: String, val mimeType: String)

fun parseProductImagesJson(json: String?): List<ProductImageData> {
  if (json.isNullOrBlank()) return emptyList()
  return try {
    val rows: List<ProductImageRow> = objectMapper.readValue(json)
    rows.mapNotNull { row ->
      S3Helper.getPublicUrl(row.s3Key)?.let { url ->
        ProductImageData(mimeType = row.mimeType, url = url)
      }
    }
  } catch (_: Exception) {
    emptyList()
  }
}

data class ProductImageData(
  val mimeType: String,
  val url: String,
)
