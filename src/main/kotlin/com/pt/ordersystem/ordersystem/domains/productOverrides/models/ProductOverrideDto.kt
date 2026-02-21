package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import java.math.BigDecimal

data class ProductOverrideDto(
  val id: Long,
  val productId: String,
  val managerId: String,
  val agentId: String?,
  val customerId: String,
  val overridePrice: BigDecimal,
)

// Separate DTO for list view with product price from JOIN
data class ProductOverrideWithPriceDto(
  val id: Long,
  val productId: String,
  val managerId: String,
  val agentId: String?,
  val customerId: String,
  val overridePrice: BigDecimal,
  val productPrice: BigDecimal,
  val productMinimumPrice: BigDecimal,
)

// Extension function to map raw SQL result to DTO
fun Array<Any>.toProductOverrideWithPriceDto() = ProductOverrideWithPriceDto(
  id = (this[0] as Number).toLong(),
  productId = this[1] as String,
  managerId = this[2] as String,
  agentId = this[3] as String?,
  customerId = this[4] as String,
  overridePrice = this[5] as BigDecimal,
  productPrice = this[6] as BigDecimal,
  productMinimumPrice = this[7] as BigDecimal,
)

