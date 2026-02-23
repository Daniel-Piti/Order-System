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
    val imageMetadata: ImageMetadata? = null,
) {
    fun normalize(): UpdateBrandRequest = this.copy(
        name = name.trim(),
    )
}

data class BrandCreateResponse(
    val brandId: Long,
    val preSignedUrl: String? = null,
)

data class BrandUpdateResponse(
    val brandId: Long,
    val preSignedUrl: String? = null,
)
