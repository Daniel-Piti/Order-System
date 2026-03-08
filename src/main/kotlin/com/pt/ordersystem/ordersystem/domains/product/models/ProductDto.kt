package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

// Simple product data for orders - V1
// When V2 is needed: create ProductDataForOrder_V2, support both temporarily, then migrate
data class ProductDataForOrder(
  val productId: String,
  val productName: String,
  val quantity: Int,
  val pricePerUnit: BigDecimal
)
