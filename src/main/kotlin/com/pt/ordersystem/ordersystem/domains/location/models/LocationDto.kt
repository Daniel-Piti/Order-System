package com.pt.ordersystem.ordersystem.domains.location.models

data class LocationDto(
    val id: Long,
    val managerId: String,
    val name: String,
    val streetAddress: String,
    val city: String,
    val phoneNumber: String,
)