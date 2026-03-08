package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class ProductPublicDto(
  val id: String,
  val managerId: String,
  val name: String,
  val brandId: Long?,
  val brandName: String?,
  val categoryId: Long?,
  val categoryName: String?,
  val price: BigDecimal,
  val description: String,
)
