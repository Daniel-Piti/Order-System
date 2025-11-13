package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDbEntity
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDto
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentFailureReason
import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.UpdateAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.toDto
import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
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

  companion object {
    private const val MAX_AGENTS_PER_MANAGER = 1000
  }

  fun getAgentsForManager(managerId: String): List<AgentDto> =
    agentRepository.findByManagerId(managerId).map { it.toDto() }

  fun getAgentProfile(agentId: String): AgentDto =
    agentRepository.findById(parseAgentId(agentId)).orElseThrow { agentNotFound("agentId=$agentId") }.toDto()

  fun getAgentEntity(agentId: String): AgentDbEntity =
    agentRepository.findById(parseAgentId(agentId)).orElseThrow { agentNotFound("agentId=$agentId") }

  fun getAgentByEmail(email: String): AgentDbEntity =
    agentRepository.findByEmail(email.trim().lowercase()) ?: throw agentNotFound("email=$email")

  fun createAgent(managerId: String, request: NewAgentRequest): Long {

    validateNewAgentRequest(request)

    val normalizedEmail = request.email.trim().lowercase()

    if (agentRepository.existsByEmail(normalizedEmail)) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = AgentFailureReason.EMAIL_ALREADY_EXISTS.userMessage,
        technicalMessage = AgentFailureReason.EMAIL_ALREADY_EXISTS.technical + "email=$normalizedEmail",
        severity = SeverityLevel.INFO,
      )
    }

    val existingAgents = agentRepository.findByManagerId(managerId).size
    if (existingAgents >= MAX_AGENTS_PER_MANAGER) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = AgentFailureReason.LIMIT_REACHED.userMessage,
        technicalMessage = AgentFailureReason.LIMIT_REACHED.technical + "managerId=$managerId, limit=$MAX_AGENTS_PER_MANAGER",
        severity = SeverityLevel.INFO,
      )
    }

    val now = LocalDateTime.now()
    val agent = AgentDbEntity(
      managerId = managerId,
      firstName = request.firstName.trim(),
      lastName = request.lastName.trim(),
      email = normalizedEmail,
      password = passwordEncoder.encode(request.password),
      phoneNumber = request.phoneNumber.trim(),
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      createdAt = now,
      updatedAt = now,
    )

    return agentRepository.save(agent).id
  }

  fun updateAgentForManager(managerId: String, agentId: Long, request: UpdateAgentRequest): AgentDto {
    validateUpdateAgentRequest(request)

    val existing = agentRepository.findByManagerIdAndId(managerId, agentId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = AgentFailureReason.NOT_FOUND.userMessage,
        technicalMessage = AgentFailureReason.NOT_FOUND.technical + "managerId=$managerId, agentId=$agentId",
        severity = SeverityLevel.WARN,
      )

    val updated = existing.copy(
      firstName = request.firstName.trim(),
      lastName = request.lastName.trim(),
      phoneNumber = request.phoneNumber.trim(),
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      updatedAt = LocalDateTime.now(),
    )

    return agentRepository.save(updated).toDto()
  }

  fun updateAgentSelf(agentId: String, request: UpdateAgentRequest): AgentDto {
    validateUpdateAgentRequest(request)

    val numericId = parseAgentId(agentId)

    val existing = agentRepository.findById(numericId).orElseThrow {
      agentNotFound("agentId=$agentId")
    }

    val updated = existing.copy(
      firstName = request.firstName.trim(),
      lastName = request.lastName.trim(),
      phoneNumber = request.phoneNumber.trim(),
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      updatedAt = LocalDateTime.now(),
    )

    return agentRepository.save(updated).toDto()
  }

  @Transactional
  fun deleteAgent(managerId: String, agentId: Long) {
    val agent = agentRepository.findByManagerIdAndId(managerId, agentId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = AgentFailureReason.NOT_FOUND.userMessage,
        technicalMessage = AgentFailureReason.NOT_FOUND.technical + "managerId=$managerId, agentId=$agentId",
        severity = SeverityLevel.WARN,
      )

    // Delete all product overrides associated with this agent
    productOverrideService.deleteAllOverridesForAgent(managerId, agentId)

    // Unassign all customers from this agent (set agentId to null)
    customerService.unassignCustomersFromAgent(managerId, agentId)

    agentRepository.delete(agent)
  }

  private fun validateNewAgentRequest(request: NewAgentRequest) {
    FieldValidators.validateNonEmpty(request.firstName, "'first name'")
    FieldValidators.validateNonEmpty(request.lastName, "'last name'")
    FieldValidators.validateEmail(request.email)
    FieldValidators.validateStrongPassword(request.password)
    FieldValidators.validatePhoneNumber(request.phoneNumber)
    FieldValidators.validateNonEmpty(request.streetAddress, "'street address'")
    FieldValidators.validateNonEmpty(request.city, "'city'")
  }

  private fun validateUpdateAgentRequest(request: UpdateAgentRequest) {
    FieldValidators.validateNonEmpty(request.firstName, "'first name'")
    FieldValidators.validateNonEmpty(request.lastName, "'last name'")
    FieldValidators.validatePhoneNumber(request.phoneNumber)
    FieldValidators.validateNonEmpty(request.streetAddress, "'street address'")
    FieldValidators.validateNonEmpty(request.city, "'city'")
  }

  private fun agentNotFound(details: String) = ServiceException(
    status = HttpStatus.NOT_FOUND,
    userMessage = AgentFailureReason.NOT_FOUND.userMessage,
    technicalMessage = AgentFailureReason.NOT_FOUND.technical + details,
    severity = SeverityLevel.WARN,
  )

  fun parseAgentId(agentId: String): Long =
    agentId.toLongOrNull() ?: throw ServiceException(
      status = HttpStatus.BAD_REQUEST,
      userMessage = "Invalid agent identifier",
      technicalMessage = "Unable to parse agentId=$agentId as Long",
      severity = SeverityLevel.WARN,
    )
}
