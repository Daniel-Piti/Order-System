package com.pt.ordersystem.ordersystem.domains.order.models

data class OrderPublicDto(
  val managerId: String,
  val status: OrderStatus,
  val customerId: String?
)

fun OrderDbEntity.toPublicDto(): OrderPublicDto {
  val orderStatus = OrderStatus.valueOf(this.status)
  return OrderPublicDto(
    managerId = this.managerId,
    status = orderStatus,
    customerId = this.customerId
  )
}
