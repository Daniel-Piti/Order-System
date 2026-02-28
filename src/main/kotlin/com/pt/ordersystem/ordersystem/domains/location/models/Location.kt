package com.pt.ordersystem.ordersystem.domains.location.models

import java.time.LocalDateTime

data class Location(
    val id: Long,
    val managerId: String,
    val name: String,
    val streetAddress: String,
    val city: String,
    val phoneNumber: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun LocationDbEntity.toModel(): Location = Location(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    streetAddress = this.streetAddress,
    city = this.city,
    phoneNumber = this.phoneNumber,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun Location.toDto(): LocationDto = LocationDto(
    id = this.id,
    managerId = this.managerId,
    name = this.name,
    streetAddress = this.streetAddress,
    city = this.city,
    phoneNumber = this.phoneNumber,
)
