package com.pt.ordersystem.ordersystem.domains.order.models

enum class OrderStatus {
  EMPTY,      // Order created, link can be shared
  PLACED,     // Order confirmed/placed by customer
  DONE,       // Order fulfilled
  EXPIRED,    // Link expired
  CANCELLED,  // Order cancelled
}