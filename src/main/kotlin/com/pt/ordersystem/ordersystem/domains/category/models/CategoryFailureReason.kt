package com.pt.ordersystem.ordersystem.domains.category.models

enum class CategoryFailureReason(
    val userMessage: String,
    val technical: String
) {
    NOT_FOUND(
        userMessage = "Category not found",
        technical = "Category not found for userId="
    ),
    ALREADY_EXISTS(
        userMessage = "Category already exists",
        technical = "Category already exists for userId="
    )
}
