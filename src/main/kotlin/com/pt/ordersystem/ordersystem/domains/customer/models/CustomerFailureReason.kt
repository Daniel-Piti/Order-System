package com.pt.ordersystem.ordersystem.domains.customer.models

enum class CustomerFailureReason {
  CUSTOMER_NOT_FOUND,
  CUSTOMER_ALREADY_EXISTS,
  INVALID_CUSTOMER_DATA,
  UNAUTHORIZED_ACCESS,
  DATABASE_ERROR
}

