package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.time.LocalDate

data class CreateEmptyOrderRequest(
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
  val deliveryDate: LocalDate? = null,
  val notes: String = ""
)
