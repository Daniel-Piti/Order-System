package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class ProductDto(
  val id: String,
  val userId: String,
  val name: String,
  val categoryId: String?,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
  val description: String,
  val pictureUrl: String?
)

data class ProductDataForOrder(
  val id: String,
  val quantity: Int,
  val price: BigDecimal,
)

fun ProductDbEntity.toDto() = ProductDto(
  id = id,
  userId = userId,
  name = name,
  categoryId = categoryId,
  originalPrice = originalPrice,
  specialPrice = specialPrice,
  description = description,
  pictureUrl = pictureUrl,
)
