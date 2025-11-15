package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class ProductDto(
  val id: String,
  val managerId: String,
  val name: String,
  val brandId: Long?,
  val brandName: String?,
  val categoryId: Long?,
  val categoryName: String?,
  val minimumPrice: BigDecimal,
  val price: BigDecimal,
  val description: String
)

// Simple product data for orders - V1
// When V2 is needed: create ProductDataForOrder_V2, support both temporarily, then migrate
data class ProductDataForOrder(
  val productId: String,
  val productName: String,
  val quantity: Int,
  val pricePerUnit: BigDecimal
)

fun ProductDbEntity.toDto(brandName: String? = null, categoryName: String? = null, price: BigDecimal? = null) = ProductDto(
  id = id,
  managerId = managerId,
  name = name,
  brandId = brandId,
  brandName = brandName,
  categoryId = categoryId,
  categoryName = categoryName,
  minimumPrice = minimumPrice,
  price = price ?: this.price,
  description = description
)
