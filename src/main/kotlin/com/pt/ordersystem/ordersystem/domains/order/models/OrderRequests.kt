package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder

data class CreateOrderRequest(
  val customerId: String? = null
)

data class PlaceOrderRequest(
  // Customer info
  val customerName: String,
  val customerPhone: String,
  val customerEmail: String?,
  val customerStreetAddress: String,
  val customerCity: String,
  
  // Pickup location
  val pickupLocationId: Long,
  
  // Order details
  val products: List<ProductDataForOrder>,
  val notes: String = ""
)

data class UpdateOrderRequest(
  // Pickup location
  val pickupLocationId: Long,
  
  // Order details (editable: products, notes)
  val products: List<ProductDataForOrder>,
  val notes: String = ""
  // Note: Customer info is NOT editable (order already placed)
)
