package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import java.math.BigDecimal

data class ProductOverrideDto(
  val id: String,
  val productId: String,
  val userId: String,
  val customerId: String,
  val overridePrice: BigDecimal,
)

