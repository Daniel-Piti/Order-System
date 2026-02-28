package com.pt.ordersystem.ordersystem.domains.manager.helpers

import com.pt.ordersystem.ordersystem.domains.manager.models.CreateManagerRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.UpdateManagerDetailsRequest
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators

object ManagerValidators {

    fun validateCreateManagerRequestFields(createManagerRequest: CreateManagerRequest) {
        with(createManagerRequest) {
            FieldValidators.validateNonEmpty(firstName, "'first name'")
            FieldValidators.validateNonEmpty(lastName, "'last name'")
            FieldValidators.validateEmail(email)
            FieldValidators.validateStrongPassword(password)
            FieldValidators.validatePhoneNumber(phoneNumber)
            FieldValidators.validateDateNotFuture(dateOfBirth)
            FieldValidators.validateNonEmpty(streetAddress, "'street address'")
            FieldValidators.validateNonEmpty(city, "'city'")
        }
    }

    fun validateUpdateManagerRequestFields(updateManagerDetailsRequest: UpdateManagerDetailsRequest) {
        with(updateManagerDetailsRequest) {
            FieldValidators.validateNonEmpty(firstName, "'first name'")
            FieldValidators.validateNonEmpty(lastName, "'last name'")
            FieldValidators.validatePhoneNumber(phoneNumber)
            FieldValidators.validateDateNotFuture(dateOfBirth)
            FieldValidators.validateNonEmpty(streetAddress, "'street address'")
            FieldValidators.validateNonEmpty(city, "'city'")
        }
    }
}