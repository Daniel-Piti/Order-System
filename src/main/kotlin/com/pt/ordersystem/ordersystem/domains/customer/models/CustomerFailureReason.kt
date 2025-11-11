package com.pt.ordersystem.ordersystem.domains.customer.models

enum class CustomerFailureReason(val userMessage: String, val technical: String) {
  CUSTOMER_NOT_FOUND(
    userMessage = "Customer not found",
    technical = "Customer not found | "
  ),
  CUSTOMER_ALREADY_EXISTS(
    userMessage = "A customer with this phone number already exists",
    technical = "Customer with same phone number already exists | "
  ),
  CUSTOMER_LIMIT_EXCEEDED(
    userMessage = "You have reached maximum customers capacity",
    technical = "Reached maximum customers limit | "
  ),
  AGENT_CUSTOMER_LIMIT_EXCEEDED(
    userMessage = "This agent already manages the maximum number of customers",
    technical = "Reached maximum customers per agent limit | "
  ),
  AGENT_NOT_FOUND(
    userMessage = "Agent not found",
    technical = "Agent not found | "
  ),
}

