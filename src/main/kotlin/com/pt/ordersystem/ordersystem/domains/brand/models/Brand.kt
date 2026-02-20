package com.pt.ordersystem.ordersystem.domains.brand.models

/**
 * Domain model for Brand. Used by the service layer.
 * Controllers map this to [BrandDto] for API responses.
 */
data class Brand(
    val id: Long,
    val managerId: String,
    val name: String,
    val imageUrl: String?,
    val fileName: String?,
    val mimeType: String?,
)

fun BrandDbEntity.toBrand(imageUrl: String?) = Brand(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    imageUrl = imageUrl,
    fileName = this.fileName,
    mimeType = this.mimeType,
)

fun Brand.toDto() = BrandDto(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    imageUrl = this.imageUrl,
    fileName = this.fileName,
    mimeType = this.mimeType,
)
