package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class ProductDto(
  val id: String,
  val userId: String,
  val name: String,
  val category: String?,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
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
  category = category,
  originalPrice = originalPrice,
  specialPrice = specialPrice,
  pictureUrl = pictureUrl,
)
