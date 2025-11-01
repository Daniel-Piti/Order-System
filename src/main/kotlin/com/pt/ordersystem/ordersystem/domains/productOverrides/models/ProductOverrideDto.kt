package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import java.math.BigDecimal

data class ProductOverrideDto(
  val id: Long,
  val productId: String,
  val userId: String,
  val customerId: String,
  val overridePrice: BigDecimal,
)

// Separate DTO for list view with product price from JOIN
data class ProductOverrideWithPriceDto(
  val id: Long,
  val productId: String,
  val userId: String,
  val customerId: String,
  val overridePrice: BigDecimal,
  val originalPrice: BigDecimal,  // Product's special price from JOIN
)

// Extension function to map raw SQL result to DTO
fun Array<Any>.toProductOverrideWithPriceDto() = ProductOverrideWithPriceDto(
  id = (this[0] as Number).toLong(),
  productId = this[1] as String,
  userId = this[2] as String,
  customerId = this[3] as String,
  overridePrice = this[4] as BigDecimal,
  originalPrice = this[5] as BigDecimal
)

