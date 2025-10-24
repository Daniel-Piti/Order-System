package com.pt.ordersystem.ordersystem.domains.productOverrides.models

enum class ProductOverrideFailureReason(val message: String) {
  PRODUCT_OVERRIDE_NOT_FOUND("Price override not found"),
  PRODUCT_OVERRIDE_ALREADY_EXISTS("A price override for this product and customer already exists"),
  PRODUCT_NOT_FOUND("Product not found"),
  CUSTOMER_NOT_FOUND("Customer not found"),
}

