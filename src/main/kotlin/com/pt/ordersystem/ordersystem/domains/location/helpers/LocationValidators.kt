package com.pt.ordersystem.ordersystem.domains.location.helpers

import com.pt.ordersystem.ordersystem.domains.location.models.LocationFailureReason
import com.pt.ordersystem.ordersystem.domains.location.models.CreateLocationRequest
import com.pt.ordersystem.ordersystem.domains.location.models.UpdateLocationRequest
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus

object LocationValidators {

    private const val MAXIMUM_LOCATIONS = 10

    fun validateMaxLocationCount(locationsCount: Long, managerId: String) {
        if (locationsCount >= MAXIMUM_LOCATIONS) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = LocationFailureReason.TOO_MANY_LOCATIONS.userMessage,
                technicalMessage = LocationFailureReason.TOO_MANY_LOCATIONS.technical + "manager=$managerId",
                severity = SeverityLevel.INFO,
            )
        }
    }

    fun validateAtLeastOneLocationExists(locationsCount: Long, managerId: String) {
        if (locationsCount <= 1) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = LocationFailureReason.CANNOT_DELETE_LAST.userMessage,
                technicalMessage = LocationFailureReason.CANNOT_DELETE_LAST.technical + "managerId=$managerId",
                severity = SeverityLevel.INFO,
            )
        }
    }

    fun validateCreateLocationRequestFields(request: CreateLocationRequest) {
        with(request) {
            FieldValidators.validateNonEmpty(name, "'name'")
            FieldValidators.validateNonEmpty(streetAddress, "'street address'")
            FieldValidators.validateNonEmpty(city, "'city'")
            FieldValidators.validatePhoneNumber(phoneNumber)
        }
    }

    fun validateUpdateLocationRequestFields(request: UpdateLocationRequest) {
        with(request) {
            FieldValidators.validateNonEmpty(name, "'name'")
            FieldValidators.validateNonEmpty(streetAddress, "'street address'")
            FieldValidators.validateNonEmpty(city, "'city'")
            FieldValidators.validatePhoneNumber(phoneNumber)
        }
    }

}