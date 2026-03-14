package com.pt.ordersystem.ordersystem.domains.category.models

import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators

data class CreateCategoryRequest(
    val name: String,
) {
    fun validateAndNormalize(): CreateCategoryRequest {
        FieldValidators.validateNonEmpty(this.name, "'name'")

        return this.copy(
            name = name.trim(),
        )
    }
}

data class UpdateCategoryRequest(
    val name: String
) {
    fun validateAndNormalize(): UpdateCategoryRequest {
        FieldValidators.validateNonEmpty(this.name, "'name'")

        return this.copy(
            name = name.trim(),
        )
    }
}
