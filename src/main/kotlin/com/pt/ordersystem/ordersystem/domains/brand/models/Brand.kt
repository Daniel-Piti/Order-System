package com.pt.ordersystem.ordersystem.domains.brand.models

import java.time.LocalDateTime

data class Brand(
    val id: Long,
    val managerId: String,
    val name: String,
    val imageUrl: String?,
    val s3Key: String?,
    val fileName: String?,
    val fileSizeBytes: Long?,
    val mimeType: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun BrandDbEntity.toModel(imageUrl: String?) = Brand(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    imageUrl = imageUrl,
    s3Key = s3Key,
    fileName = this.fileName,
    fileSizeBytes = this.fileSizeBytes,
    mimeType = this.mimeType,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun Brand.toDto() = BrandDto(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    imageUrl = this.imageUrl,
    fileName = this.fileName,
    mimeType = this.mimeType,
)
