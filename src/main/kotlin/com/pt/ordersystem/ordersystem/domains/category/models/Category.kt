package com.pt.ordersystem.ordersystem.domains.category.models

import java.time.LocalDateTime

data class Category(
    val id: Long,
    val managerId: String,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun CategoryDbEntity.toModel(): Category = Category(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun Category.toDto(): CategoryDto = CategoryDto(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
)
