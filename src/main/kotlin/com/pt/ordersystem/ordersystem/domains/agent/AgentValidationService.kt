package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.domains.agent.helpers.AgentValidators
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDbEntity
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentFailureReason
import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AgentValidationService(
    private val agentRepository: AgentRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    ) {
    fun validateCreateAgent(request: NewAgentRequest, managerId: String) {
        val agentCount = agentRepository.countByManagerId(managerId)
        AgentValidators.validateMaxAgentsNumber(agentCount, managerId)

        if (agentRepository.existsByEmail(request.email)) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = AgentFailureReason.EMAIL_ALREADY_EXISTS.userMessage,
                technicalMessage = AgentFailureReason.EMAIL_ALREADY_EXISTS.technical + "email=${request.email}",
                severity = SeverityLevel.INFO,
            )
        }
    }

    fun validateAgentOfManager(agentId: String, managerId: String) {
        val existing = agentRepository.findEntityById(agentId)
        if (existing.managerId != managerId) {
            throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = AgentFailureReason.NOT_FOUND.userMessage,
                technicalMessage = AgentFailureReason.NOT_FOUND.technical + "managerId=$managerId, agentId=$agentId",
                severity = SeverityLevel.WARN,
            )
        }
    }

    fun validateUpdatePassword(
        agent: AgentDbEntity,
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ) {
        FieldValidators.validateNewPasswordEqualConfirmationPassword(newPassword, newPasswordConfirmation)
        FieldValidators.validateNewPasswordNotEqualOldPassword(oldPassword, newPassword)
        FieldValidators.validateStrongPassword(newPassword)

        if (!passwordEncoder.matches(oldPassword, agent.password)) {
            throw ServiceException(
                status = HttpStatus.UNAUTHORIZED,
                userMessage = "Old password is incorrect",
                technicalMessage = "Password mismatch for agent with id=${agent.id}",
                severity = SeverityLevel.WARN
            )
        }
    }
}