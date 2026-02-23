package com.pt.ordersystem.ordersystem.domains.business.models

import java.time.LocalDateTime

data class BusinessDto(
    val id: String,
    val managerId: String,
    val name: String,
    val stateIdNumber: String,
    val email: String,
    val phoneNumber: String,
    val streetAddress: String,
    val city: String,
    val imageUrl: String?,
    val fileName: String?,
    val mimeType: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class PublicBusinessDto(
    val name: String,
    val imageUrl: String?,
)

fun BusinessDto.toPublicDto(): PublicBusinessDto = PublicBusinessDto(
    name = this.name,
    imageUrl = this.imageUrl,
)
