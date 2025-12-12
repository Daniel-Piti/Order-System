package com.pt.ordersystem.ordersystem.domains.brand.models

import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata

data class CreateBrandRequest(
    val name: String,
    val imageMetadata: ImageMetadata? = null,
)

data class UpdateBrandRequest(
    val name: String,
    val imageMetadata: ImageMetadata? = null,
)

data class BrandCreateResponse(
    val brandId: Long,
    val preSignedUrl: String? = null,
)

data class BrandUpdateResponse(
    val brandId: Long,
    val preSignedUrl: String? = null,
)
