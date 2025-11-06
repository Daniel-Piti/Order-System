package com.pt.ordersystem.ordersystem.domains.productImage.models

data class ProductImageDto(
  val id: Long,
  val productId: String,
  val userId: String,
  val url: String,
  val fileName: String,
  val mimeType: String,
)

fun ProductImageDbEntity.toDto(publicUrl: String) = ProductImageDto(
  id = id,
  productId = productId,
  userId = userId,
  url = publicUrl,
  fileName = fileName,
  mimeType = mimeType
)

