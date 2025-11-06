package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class CreateProductRequest(
  val name: String,
  val brandId: Long?,
  val categoryId: Long?,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
  val description: String,
)

data class UpdateProductRequest(
  val name: String,
  val brandId: Long?,
  val categoryId: Long?,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
  val description: String,
)
