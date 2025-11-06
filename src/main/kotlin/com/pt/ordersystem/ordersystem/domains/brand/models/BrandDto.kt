package com.pt.ordersystem.ordersystem.domains.brand.models

data class BrandDto(
    val id: Long,
    val userId: String,
    val name: String,
    val imageUrl: String?,
    val fileName: String?,
    val mimeType: String?,
)

fun BrandDbEntity.toDto(imageUrl: String? = null) = BrandDto(
    id = this.id,
    userId = this.userId,
    name = this.name,
    imageUrl = imageUrl,
    fileName = this.fileName,
    mimeType = this.mimeType
)
