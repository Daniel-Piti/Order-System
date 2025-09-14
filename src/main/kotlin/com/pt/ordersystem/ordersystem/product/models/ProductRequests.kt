package com.pt.ordersystem.ordersystem.product.models

import java.math.BigDecimal

data class CreateProductRequest(
  val name: String,
  val category: String,
  val originalPrice: BigDecimal,
  val specialPrice: BigDecimal,
  val pictureUrl: String? = null
)

data class UpdateProductRequest(
  val name: String? = null,
  val originalPrice: BigDecimal? = null,
  val specialPrice: BigDecimal? = null,
  val pictureUrl: String? = null
)
