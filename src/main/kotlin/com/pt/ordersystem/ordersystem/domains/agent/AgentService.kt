package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.domains.agent.helpers.AgentValidators
import com.pt.ordersystem.ordersystem.domains.agent.models.Agent
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDto
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDbEntity
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentFailureReason
import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.UpdateAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.toDto
import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AgentService(
  private val agentRepository: AgentRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
  private val productOverrideService: ProductOverrideService,
  private val customerService: CustomerService,
) {

  fun getAgentsForManager(managerId: String): List<AgentDto> =
    agentRepository.findByManagerId(managerId).map { it.toDto() }

  fun getAgentProfile(agentId: String): AgentDto =
    agentRepository.findById(agentId).toDto()

  fun getAgentEntity(agentId: String): AgentDbEntity =
    agentRepository.findEntityById(agentId)

  fun getAgentByEmail(email: String): AgentDbEntity =
    agentRepository.findEntityByEmail(email)

  fun validateCreateAgent(request: NewAgentRequest, managerId: String) {
    AgentValidators.validateNewAgentRequest(request)

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

  fun createAgent(managerId: String, request: NewAgentRequest): Agent {

    val now = LocalDateTime.now()

    val agent = AgentDbEntity(
      id = GeneralUtils.genId(),
      managerId = managerId,
      firstName = request.firstName,
      lastName = request.lastName,
      email = request.email,
      password = passwordEncoder.encode(request.password),
      phoneNumber = request.phoneNumber,
      streetAddress = request.streetAddress,
      city = request.city,
      createdAt = now,
      updatedAt = now,
    )

    return agentRepository.save(agent)
  }

  fun validateAgentOfManager(agentId: String, managerId: String): Unit {
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

  fun updateAgent(agentId: String, request: UpdateAgentRequest): Agent {
    val existing = agentRepository.findEntityById(agentId)

    val updated = existing.copy(
      firstName = request.firstName.trim(),
      lastName = request.lastName.trim(),
      phoneNumber = request.phoneNumber.trim(),
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      updatedAt = LocalDateTime.now(),
    )

    return agentRepository.save(updated)
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

  @Transactional
  fun updatePassword(
    agentId: String,
    oldPassword: String,
    newPassword: String,
    newPasswordConfirmation: String
  ) {
    val agent = agentRepository.findEntityById(agentId)

    validateUpdatePassword(agent, oldPassword, newPassword, newPasswordConfirmation)

    val updatedAgent = agent.copy(
      password = passwordEncoder.encode(newPassword),
      updatedAt = LocalDateTime.now()
    )
    agentRepository.save(updatedAgent)
  }

  @Transactional
  fun deleteAgent(managerId: String, agentId: String) {
    validateAgentOfManager(agentId, managerId)

    // Delete all product overrides associated with this agent
    productOverrideService.deleteAllOverridesForAgent(managerId, agentId)

    // Unassign all customers from this agent (set agentId to null)
    customerService.unassignCustomersFromAgent(managerId, agentId)

    agentRepository.deleteById(agentId)
  }
}
