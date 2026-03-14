package com.pt.ordersystem.ordersystem.domains.manager.models

import com.pt.ordersystem.ordersystem.domains.manager.helpers.ManagerValidators
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
    fun validateAndNormalize(): CreateManagerRequest {
        ManagerValidators.validateCreateManagerRequestFields(this)

        return copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim().lowercase(),
            phoneNumber = phoneNumber.trim(),
            streetAddress = streetAddress.trim(),
            city = city.trim(),
        )
    }
}

data class UpdateManagerDetailsRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val dateOfBirth: LocalDate,
    val streetAddress: String,
    val city: String,
) {
    fun validateAndNormalize(): UpdateManagerDetailsRequest {
        ManagerValidators.validateUpdateManagerRequestFields(this)

        return copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phoneNumber = phoneNumber.trim(),
            streetAddress = streetAddress.trim(),
            city = city.trim(),
        )
    }
}
