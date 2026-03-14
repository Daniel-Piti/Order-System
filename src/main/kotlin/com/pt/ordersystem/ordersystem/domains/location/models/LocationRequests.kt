package com.pt.ordersystem.ordersystem.domains.location.models

import com.pt.ordersystem.ordersystem.domains.location.helpers.LocationValidators

data class CreateLocationRequest(
    val name: String,
    val streetAddress: String,
    val city: String,
    val phoneNumber: String,
) {
    fun validateAndNormalize(): CreateLocationRequest {
        LocationValidators.validateCreateLocationRequestFields(this)

        return copy(
            name = name.trim(),
            streetAddress = streetAddress.trim(),
            city = city.trim(),
            phoneNumber = phoneNumber.trim(),
        )
    }
}

data class UpdateLocationRequest(
    val name: String,
    val streetAddress: String,
    val city: String,
    val phoneNumber: String,
) {
    fun validateAndNormalize(): UpdateLocationRequest {
        LocationValidators.validateUpdateLocationRequestFields(this)

        return this.copy(
            name = name.trim(),
            streetAddress = streetAddress.trim(),
            city = city.trim(),
            phoneNumber = phoneNumber.trim(),
        )
    }
}