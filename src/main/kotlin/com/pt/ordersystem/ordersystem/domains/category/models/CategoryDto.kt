package com.pt.ordersystem.ordersystem.domains.category.models

data class CategoryDto(
    val id: Long,
    val managerId: String,
    val category: String
)

fun CategoryDbEntity.toDto(): CategoryDto = CategoryDto(
    id = this.id,
    managerId = this.managerId,
    category = this.category
)
