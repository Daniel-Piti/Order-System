package com.pt.ordersystem.ordersystem.domains.category.models

data class CreateCategoryRequest(
    val category: String,
) {
    fun normalize(): CreateCategoryRequest = this.copy(
        category = category.trim(),
    )
}

data class UpdateCategoryRequest(
    val category: String
) {
    fun normalize(): UpdateCategoryRequest = this.copy(
        category = category.trim(),
    )
}
