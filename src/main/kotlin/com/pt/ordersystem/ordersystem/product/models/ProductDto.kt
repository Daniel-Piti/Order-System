package com.pt.ordersystem.ordersystem.product.models

import java.math.BigDecimal

data class ProductDto(
  val id: String,
  val name: String,
  val price: BigDecimal,
  val picture: String?,
)

data class ProductDataForOrder(
  val id: String,
  val quantity: Int,
  val price: BigDecimal,
)

fun ProductDbEntity.toDto() = ProductDto(
  id = id,
  name = name,
  price = price,
  picture = picture
)
