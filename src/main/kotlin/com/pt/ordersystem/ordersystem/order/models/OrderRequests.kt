package com.pt.ordersystem.ordersystem.order.models

import com.pt.ordersystem.ordersystem.product.models.ProductDataForOrder

data class CreateOrderRequest(
  val locationId: String,
  val customerName: String,
  val customerPhone: String,
  val customerCity: String,
  val customerAddress: String,
  val products: List<ProductDataForOrder>? = null
)

data class UpdateOrderRequest(
  val customerName: String? = null,
  val customerPhone: String? = null,
  val customerCity: String? = null,
  val customerAddress: String? = null,
  val status: String? = null,
  val products: List<ProductDataForOrder>? = null,
  val totalPrice: java.math.BigDecimal? = null
)
