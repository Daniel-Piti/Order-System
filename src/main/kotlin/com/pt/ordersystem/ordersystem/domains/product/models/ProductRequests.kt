package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class CreateProductRequest(
  val name: String,
  val category: String?,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
  val pictureUrl: String,
)

data class UpdateProductRequest(
  val name: String,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
  val pictureUrl: String,
)
