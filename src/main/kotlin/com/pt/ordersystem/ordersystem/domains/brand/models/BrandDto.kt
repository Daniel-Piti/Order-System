package com.pt.ordersystem.ordersystem.domains.brand.models

data class BrandDto(
    val id: Long,
    val userId: String,
    val name: String,
    val s3Key: String?,
    val fileName: String?,
    val fileSizeBytes: Long?,
    val mimeType: String?
)

fun BrandDbEntity.toDto(): BrandDto = BrandDto(
    id = this.id,
    userId = this.userId,
    name = this.name,
    s3Key = this.s3Key,
    fileName = this.fileName,
    fileSizeBytes = this.fileSizeBytes,
    mimeType = this.mimeType
)
