package com.pt.ordersystem.ordersystem.domains.order.models

data class OrderPublicDto(
  val managerId: String,
  val referenceId: Long,
  val status: OrderStatus,
  val customerId: String?,
)
