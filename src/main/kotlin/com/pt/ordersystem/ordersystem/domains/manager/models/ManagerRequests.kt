package com.pt.ordersystem.ordersystem.domains.manager.models

import java.time.LocalDate

data class CreateManagerRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val dateOfBirth: LocalDate,
    val streetAddress: String,
    val city: String,
) {
    fun normalize(): CreateManagerRequest = copy(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        email = email.trim().lowercase(),
        phoneNumber = phoneNumber.trim(),
        streetAddress = streetAddress.trim(),
        city = city.trim(),
    )
}

data class UpdateManagerDetailsRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val dateOfBirth: LocalDate,
    val streetAddress: String,
    val city: String,
) {
    fun normalize(): UpdateManagerDetailsRequest = copy(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        phoneNumber = phoneNumber.trim(),
        streetAddress = streetAddress.trim(),
        city = city.trim(),
    )
}
