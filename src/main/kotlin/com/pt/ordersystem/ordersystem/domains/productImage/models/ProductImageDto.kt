package com.pt.ordersystem.ordersystem.domains.productImage.models

import com.pt.ordersystem.ordersystem.storage.S3Helper

data class ProductImageDto(
  val id: Long,
  val productId: String,
  val managerId: String,
  val url: String,
  val fileName: String,
  val mimeType: String,
)

fun ProductImageDbEntity.toDto() = ProductImageDto(
  id = id,
  productId = productId,
  managerId = managerId,
  url = S3Helper.getPublicUrl(s3Key) ?: "",
  fileName = fileName,
  mimeType = mimeType
)

