package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.domains.agent.models.Agent
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDto
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDbEntity
import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.UpdateAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.toDto
import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
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
  private val agentValidationService: AgentValidationService,
) {

  fun getAgentsForManager(managerId: String): List<AgentDto> =
    agentRepository.findByManagerId(managerId).map { it.toDto() }

  fun getAgent(agentId: String): Agent =
    agentRepository.findById(agentId)

  fun getAgentByEmail(email: String): AgentDbEntity =
    agentRepository.findEntityByEmail(email)

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

  fun updateAgent(agentId: String, request: UpdateAgentRequest): Agent {
    val existing = agentRepository.findEntityById(agentId)

    val updated = existing.copy(
      firstName = request.firstName,
      lastName = request.lastName,
      phoneNumber = request.phoneNumber,
      streetAddress = request.streetAddress,
      city = request.city,
      updatedAt = LocalDateTime.now(),
    )

    return agentRepository.save(updated)
  }

  @Transactional
  fun updatePassword(
    agentId: String,
    oldPassword: String,
    newPassword: String,
    newPasswordConfirmation: String
  ) {
    val agent = agentRepository.findEntityById(agentId)

    agentValidationService.validateUpdatePassword(agent, oldPassword, newPassword, newPasswordConfirmation)

    val updatedAgent = agent.copy(
      password = passwordEncoder.encode(newPassword),
      updatedAt = LocalDateTime.now()
    )
    agentRepository.save(updatedAgent)
  }

  @Transactional
  fun deleteAgent(managerId: String, agentId: String) {
    agentValidationService.validateAgentOfManager(agentId, managerId)

    // Delete all product overrides associated with this agent
    productOverrideService.deleteAllOverridesForAgent(managerId, agentId)

    // Delete all customers assigned to this agent
    customerService.deleteAllCustomersForAgent(managerId, agentId)

    agentRepository.deleteById(agentId)
  }
}
