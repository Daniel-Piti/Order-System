package com.pt.ordersystem.ordersystem.domains.order.models

data class OrderPublicDto(
  val userId: String,
  val status: OrderStatus,
  val customerId: String? // Needed for price overrides when fetching products
)

fun OrderDbEntity.toPublicDto(): OrderPublicDto {
  val orderStatus = OrderStatus.valueOf(this.status)
  return OrderPublicDto(
    userId = this.userId,
    status = orderStatus,
    customerId = this.customerId
  )
}

