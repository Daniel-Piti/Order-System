package com.pt.ordersystem.ordersystem.product.models

import java.math.BigDecimal

data class ProductRequest(
  val name: String,
  val price: BigDecimal,
  val picture: String?
)
