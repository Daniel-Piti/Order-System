package com.pt.ordersystem.ordersystem.domains.brand.models

enum class BrandFailureReason(
    val userMessage: String,
    val technical: String
) {
    NOT_FOUND(
        userMessage = "Brand not found",
        technical = "Brand not found for managerId="
    ),
    ALREADY_EXISTS(
        userMessage = "Brand already exists",
        technical = "Brand already exists for managerId="
    ),
    BRAND_LIMIT_EXCEEDED(
        userMessage = "Brand limit exceeded",
        technical = "Manager has reached the maximum limit of brands. managerId="
    )
}
