package com.pt.ordersystem.ordersystem.location

enum class LocationFailureReason(val userMessage: String, val technical: String) {
  NOT_FOUND(
    userMessage = "Location not found",
    technical = "Location not found | "
  ),
  TWO_MANY_LOCATIONS(
    userMessage = "You have reached maximum locations capacity",
    technical = "Reached to maximum locations | "
  )
}