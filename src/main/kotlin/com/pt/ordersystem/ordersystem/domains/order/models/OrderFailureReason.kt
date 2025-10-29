package com.pt.ordersystem.ordersystem.domains.order.models

enum class OrderFailureReason(val userMessage: String, val technical: String) {
  NOT_FOUND(
    userMessage = "Order not found",
    technical = "Order not found | "
  ),
  EXPIRED(
    userMessage = "This order link has expired",
    technical = "Order link expired | "
  ),
  UNAUTHORIZED(
    userMessage = "You don't have permission to access this order",
    technical = "Unauthorized order access | "
  ),
  INVALID_STATUS(
    userMessage = "Cannot perform this action with current order status",
    technical = "Invalid order status transition | "
  ),
  NO_LOCATIONS(
    userMessage = "Cannot create order. Please add at least one location first.",
    technical = "User attempted to create order with 0 locations | "
  ),
}
