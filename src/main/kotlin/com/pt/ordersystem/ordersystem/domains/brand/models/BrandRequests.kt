package com.pt.ordersystem.ordersystem.domains.brand.models

import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata

data class CreateBrandRequest(
    val name: String,
    val imageMetadata: ImageMetadata? = null,
) {
    fun normalize(): CreateBrandRequest = this.copy(
        name = name.trim(),
    )
}

data class UpdateBrandRequest(
    val name: String,
) {
    fun normalize(): UpdateBrandRequest = this.copy(
        name = name.trim(),
    )
}

data class CreateBrandResponse(
    val brand: BrandDto,
    val preSignedUrl: String? = null,
)

data class UpdateBrandNameResponse(
    val brand: BrandDto,
)

data class UpdateBrandImageResponse(
    val brand: BrandDto,
    val preSignedUrl: String,
)
