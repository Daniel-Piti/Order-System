package com.pt.ordersystem.ordersystem.domains.location.models

data class CreateLocationRequest(
    val name: String,
    val streetAddress: String,
    val city: String,
    val phoneNumber: String,
) {
    fun normalize(): CreateLocationRequest = copy(
        name = name.trim(),
        streetAddress = streetAddress.trim(),
        city = city.trim(),
        phoneNumber = phoneNumber.trim(),
    )
}

data class UpdateLocationRequest(
    val name: String,
    val streetAddress: String,
    val city: String,
    val phoneNumber: String,
) {
    fun normalize(): UpdateLocationRequest = copy(
        name = name.trim(),
        streetAddress = streetAddress.trim(),
        city = city.trim(),
        phoneNumber = phoneNumber.trim(),
    )
}