package com.pt.ordersystem.ordersystem.domains.agent.helpers

import com.pt.ordersystem.ordersystem.domains.agent.models.AgentFailureReason
import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.UpdateAgentRequest
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus

object AgentValidators {

    private const val MAX_AGENTS_PER_MANAGER = 10

    fun validateMaxAgentsNumber(agentCount: Long, managerId: String) {
        if (agentCount >= MAX_AGENTS_PER_MANAGER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = AgentFailureReason.LIMIT_REACHED.userMessage,
                technicalMessage = AgentFailureReason.LIMIT_REACHED.technical + "managerId=$managerId, limit=$MAX_AGENTS_PER_MANAGER",
                severity = SeverityLevel.INFO,
            )
        }
    }

    fun validateNewAgentRequest(request: NewAgentRequest) {
        FieldValidators.validateNonEmpty(request.firstName, "'first name'")
        FieldValidators.validateNonEmpty(request.lastName, "'last name'")
        FieldValidators.validateEmail(request.email)
        FieldValidators.validateStrongPassword(request.password)
        FieldValidators.validatePhoneNumber(request.phoneNumber)
        FieldValidators.validateNonEmpty(request.streetAddress, "'street address'")
        FieldValidators.validateNonEmpty(request.city, "'city'")
    }

    fun validateUpdateAgentRequest(request: UpdateAgentRequest) {
        FieldValidators.validateNonEmpty(request.firstName, "'first name'")
        FieldValidators.validateNonEmpty(request.lastName, "'last name'")
        FieldValidators.validatePhoneNumber(request.phoneNumber)
        FieldValidators.validateNonEmpty(request.streetAddress, "'street address'")
        FieldValidators.validateNonEmpty(request.city, "'city'")
    }

}