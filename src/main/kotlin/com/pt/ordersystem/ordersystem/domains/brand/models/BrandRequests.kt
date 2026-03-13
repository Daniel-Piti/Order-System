package com.pt.ordersystem.ordersystem.domains.brand.models

import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata

data class CreateBrandRequest(
    val name: String,
    val imageMetadata: ImageMetadata? = null,
) {
    fun validateAndNormalize(): CreateBrandRequest {
        FieldValidators.validateNonEmpty(this.name, "'name'")

        return this.copy(
            name = name.trim(),
        )
    }
}

data class UpdateBrandRequest(
    val name: String,
) {
    fun validateAndNormalize(): UpdateBrandRequest {
        FieldValidators.validateNonEmpty(this.name, "'name'")

        return this.copy(
            name = name.trim(),
        )
    }
}

data class CreateBrandResponse(
    val brand: BrandDto,
    val preSignedUrl: String? = null,
)

data class UpdateBrandImageResponse(
    val brand: BrandDto,
    val preSignedUrl: String,
)
