package com.pt.ordersystem.ordersystem.domains.category.models

import java.time.LocalDateTime

data class Category(
    val id: Long,
    val managerId: String,
    val category: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun CategoryDbEntity.toModel(): Category = Category(
    id = this.id,
    managerId = this.managerId,
    category = this.category,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun Category.toDto(): CategoryDto = CategoryDto(
    id = this.id,
    managerId = this.managerId,
    category = this.category,
)
