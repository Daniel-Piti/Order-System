package com.pt.ordersystem.ordersystem.domains.customer.validators

import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerFailureReason
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerPayload
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus

object CustomerValidators {

    private const val MAX_CUSTOMER_CAP = 100

    fun validateCustomersCap(
        customerCount: Long,
        managerId: String,
        agentId: String?,
    ) {
        if (customerCount >= MAX_CUSTOMER_CAP) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = CustomerFailureReason.CUSTOMER_LIMIT_EXCEEDED.userMessage,
                technicalMessage = CustomerFailureReason.CUSTOMER_LIMIT_EXCEEDED.technical + "managerId=$managerId, agentId=$agentId",
                severity = SeverityLevel.WARN,
            )
        }
    }

    fun validatePayload(customerPayload: CustomerPayload) {
        FieldValidators.validateNonEmpty(customerPayload.name, "'name'")
        FieldValidators.validatePhoneNumber(customerPayload.phoneNumber)
        FieldValidators.validateEmail(customerPayload.email)
        FieldValidators.validateNonEmpty(customerPayload.streetAddress, "'street address'")
        FieldValidators.validateNonEmpty(customerPayload.city, "'city'")
        FieldValidators.validateNumericString(customerPayload.stateId, 9, "Customer state ID")
    }

}