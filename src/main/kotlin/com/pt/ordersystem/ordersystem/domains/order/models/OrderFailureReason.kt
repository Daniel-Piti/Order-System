package com.pt.ordersystem.ordersystem.domains.order.models

enum class OrderFailureReason(val userMessage: String, val technical: String) {
  NOT_FOUND(
    userMessage = "Order not found",
    technical = "Order not found | "
  ),
}
