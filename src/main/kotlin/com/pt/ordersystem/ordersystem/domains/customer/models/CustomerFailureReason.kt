package com.pt.ordersystem.ordersystem.domains.customer.models

enum class CustomerFailureReason(val userMessage: String, val technical: String) {
  CUSTOMER_NOT_FOUND(
    userMessage = "Customer not found",
    technical = "Customer not found | "
  ),
  CUSTOMER_LIMIT_EXCEEDED(
    userMessage = "You have reached maximum customers capacity",
    technical = "Reached maximum customers limit | "
  ),
}

