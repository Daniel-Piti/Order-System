package com.pt.ordersystem.ordersystem.domains.order.models

enum class OrderFailureReason(val userMessage: String, val technical: String) {
  NOT_FOUND(
    userMessage = "Order not found",
    technical = "Order not found | "
  ),
  NO_LOCATIONS(
    userMessage = "You must have at least one location to create an order",
    technical = "User attempted to create order without any locations | "
  ),
}
