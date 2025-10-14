package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import java.math.BigDecimal

data class CreateProductOverrideRequest(
  val productId: String,
  val customerId: String,
  val overridePrice: BigDecimal
)

data class UpdateProductOverrideRequest(
  val overridePrice: BigDecimal
)

