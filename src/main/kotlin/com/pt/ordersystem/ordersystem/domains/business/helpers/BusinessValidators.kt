package com.pt.ordersystem.ordersystem.domains.business.helpers

import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessDetailsRequest
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators

object BusinessValidators {

    fun validateCreateBusinessFields(request: CreateBusinessRequest) {
        with(request) {
            FieldValidators.validateNonEmpty(managerId, "'manager id'")
            FieldValidators.validateNonEmpty(name, "'name'")
            FieldValidators.validateNonEmpty(stateIdNumber, "'state id number'")
            FieldValidators.validateEmail(email)
            FieldValidators.validatePhoneNumber(phoneNumber)
            FieldValidators.validateNonEmpty(streetAddress, "'street address'")
            FieldValidators.validateNonEmpty(city, "'city'")
        }
    }

    fun validateUpdateBusinessFields(request: UpdateBusinessDetailsRequest) {
        with(request) {
            FieldValidators.validateNonEmpty(name, "'name'")
            FieldValidators.validateNonEmpty(stateIdNumber, "'state id number'")
            FieldValidators.validateEmail(email)
            FieldValidators.validatePhoneNumber(phoneNumber)
            FieldValidators.validateNonEmpty(streetAddress, "'street address'")
            FieldValidators.validateNonEmpty(city, "'city'")
        }
    }

}