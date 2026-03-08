package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class ProductPrivateDto(
  val id: String,
  val managerId: String,
  val name: String,
  val brandId: Long?,
  val brandName: String?,
  val categoryId: Long?,
  val categoryName: String?,
  val minimumPrice: BigDecimal,
  val price: BigDecimal,
  val description: String,
  val images: List<ProductImageData> = emptyList(),
)
