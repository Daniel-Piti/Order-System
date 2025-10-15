package com.pt.ordersystem.ordersystem.domains.category.models

data class CategoryDto(
    val id: String,
    val userId: String,
    val category: String
)

fun CategoryDbEntity.toDto(): CategoryDto = CategoryDto(
    id = this.id,
    userId = this.userId,
    category = this.category
)
