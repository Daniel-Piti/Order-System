package com.pt.ordersystem.ordersystem.domains.business.models

import java.time.LocalDateTime

data class Business(
    val id: String,
    val managerId: String,
    val name: String,
    val stateIdNumber: String,
    val email: String,
    val phoneNumber: String,
    val streetAddress: String,
    val city: String,
    val s3Key: String?,
    val fileName: String?,
    val fileSizeBytes: Long?,
    val mimeType: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun BusinessDbEntity.toModel(): Business = Business(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    stateIdNumber = this.stateIdNumber,
    email = this.email,
    phoneNumber = this.phoneNumber,
    streetAddress = this.streetAddress,
    city = this.city,
    s3Key = this.s3Key,
    fileName = this.fileName,
    fileSizeBytes = this.fileSizeBytes,
    mimeType = this.mimeType,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun Business.toDto(imageUrl: String?): BusinessDto =
    BusinessDto(
        id = this.id,
        managerId = this.managerId,
        name = this.name,
        stateIdNumber = this.stateIdNumber,
        email = this.email,
        phoneNumber = this.phoneNumber,
        streetAddress = this.streetAddress,
        city = this.city,
        imageUrl = imageUrl,
        fileName = this.fileName,
        mimeType = this.mimeType,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
