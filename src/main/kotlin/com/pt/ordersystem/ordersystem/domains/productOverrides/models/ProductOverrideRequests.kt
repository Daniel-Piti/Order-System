package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import java.math.BigDecimal

data class CreateProductOverrideRequest(
  val productId: String,
  val customerId: String,
  val overridePrice: BigDecimal
) {
  fun normalize(): CreateProductOverrideRequest = copy(
    productId = productId.trim(),
    customerId = customerId.trim(),
  )
}

data class UpdateProductOverrideRequest(
  val overridePrice: BigDecimal
)

