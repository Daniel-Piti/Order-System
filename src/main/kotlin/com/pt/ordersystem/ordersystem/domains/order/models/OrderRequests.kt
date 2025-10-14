package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.math.BigDecimal

data class CreateEmptyOrderRequest(
  val customerId: String?,
)

data class UpdateOrderRequest(
  val locationId: String? = null,
  val customerName: String? = null,
  val customerPhone: String? = null,
  val customerCity: String? = null,
  val customerAddress: String? = null,
  val status: String? = null,
  val products: List<ProductDataForOrder>? = null,
  val totalPrice: BigDecimal? = null
)
