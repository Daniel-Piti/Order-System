package com.pt.ordersystem.ordersystem.domains.location.models

enum class LocationFailureReason(val userMessage: String, val technical: String) {
    NOT_FOUND(
        userMessage = "Location not found",
        technical = "Location not found | "
    ),
    TOO_MANY_LOCATIONS(
        userMessage = "You have reached maximum locations capacity",
        technical = "Reached maximum locations | "
    ),
    CANNOT_DELETE_LAST(
        userMessage = "Cannot delete the last location. You must have at least one location.",
        technical = "Manager attempted to delete their last location | "
    ),
}