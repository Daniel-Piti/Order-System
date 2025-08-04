package com.pt.ordersystem.ordersystem.product

enum class ProductFailureReason(val userMessage: String, val technical: String) {
  NOT_FOUND(
    userMessage = "Product not found",
    technical = "Product not found | "
  ),
}