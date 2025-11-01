package com.pt.ordersystem.ordersystem.domains.productImage.models

data class ProductImageDto(
  val id: Long,
  val productId: String,
  val userId: String,
  val url: String, // Full public URL from R2
  val fileName: String,
  val fileSizeBytes: Long,
  val mimeType: String
)

fun ProductImageDbEntity.toDto(publicUrl: String) = ProductImageDto(
  id = id,
  productId = productId,
  userId = userId,
  url = publicUrl,
  fileName = fileName,
  fileSizeBytes = fileSizeBytes,
  mimeType = mimeType
)

