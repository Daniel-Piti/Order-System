package com.pt.ordersystem.ordersystem.product

import java.math.BigDecimal

data class ProductDto(
  val id: String,
  val name: String,
  val price: BigDecimal,
  val picture: String?,
)

data class ProductRequest(
  val name: String,
  val price: BigDecimal,
  val picture: String?
)

fun ProductDbEntity.toDto() = ProductDto(
  id = id,
  name = name,
  price = price,
  picture = picture
)