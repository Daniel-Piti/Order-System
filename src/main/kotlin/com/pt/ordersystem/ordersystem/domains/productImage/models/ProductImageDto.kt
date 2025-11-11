package com.pt.ordersystem.ordersystem.domains.productImage.models

data class ProductImageDto(
  val id: Long,
  val productId: String,
  val managerId: String,
  val url: String,
  val fileName: String,
  val mimeType: String,
)

fun ProductImageDbEntity.toDto(publicUrl: String) = ProductImageDto(
  id = id,
  productId = productId,
  managerId = managerId,
  url = publicUrl,
  fileName = fileName,
  mimeType = mimeType
)

