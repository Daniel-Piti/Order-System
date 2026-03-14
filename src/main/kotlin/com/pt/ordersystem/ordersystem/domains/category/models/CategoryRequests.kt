package com.pt.ordersystem.ordersystem.domains.category.models

import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators

data class CreateCategoryRequest(
    val category: String,
) {
    fun validateAndNormalize(): CreateCategoryRequest {
        FieldValidators.validateNonEmpty(this.category, "'category'")

        return this.copy(
            category = category.trim(),
        )
    }
}

data class UpdateCategoryRequest(
    val category: String
) {
    fun validateAndNormalize(): UpdateCategoryRequest {
        FieldValidators.validateNonEmpty(this.category, "'category'")

        return this.copy(
            category = category.trim(),
        )
    }
}
