package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.agent.AgentRepository
import com.pt.ordersystem.ordersystem.domains.customer.models.*
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CustomerService(
  private val customerRepository: CustomerRepository,
  private val productOverrideRepository: ProductOverrideRepository,
  private val agentRepository: AgentRepository,
) {

  companion object {
    private const val MAX_CUSTOMER_CAP = 100
  }

  fun getCustomers(managerId: String, agentId: Long?): List<CustomerDto> {
    val customers = if (agentId == null) {
      customerRepository.findByManagerId(managerId)
    } else {
      validateAgentExists(managerId, agentId)
      customerRepository.findByManagerIdAndAgentId(managerId, agentId)
    }
    return customers.map { it.toDto() }
  }

  fun getCustomerDto(managerId: String, customerId: String) =
    getCustomer(managerId, agentId = null, customerId = customerId).toDto()

  fun getCustomerDtoForAgent(managerId: String, agentId: Long, customerId: String) =
    getCustomer(managerId, agentId, customerId).toDto()

  @Transactional
  fun createCustomer(managerId: String, agentId: Long?, customerPayload: CustomerPayload): CustomerDto {
    val normalizedPayload = normalizePayload(customerPayload)

    // Validations
    validatePayload(normalizedPayload)
    agentId?.also { validateAgentExists(managerId, agentId) }
    validateUniquePhoneNumber(managerId, normalizedPayload.phoneNumber, excludeCustomerId = null)
    validateCustomerCap(managerId, agentId)

    val now = LocalDateTime.now()

    val customer = CustomerDbEntity(
      id = GeneralUtils.genId(),
      agentId = agentId,
      managerId = managerId,
      name = normalizedPayload.name,
      phoneNumber = normalizedPayload.phoneNumber,
      email = normalizedPayload.email,
      streetAddress = normalizedPayload.streetAddress,
      city = normalizedPayload.city,
      createdAt = now,
      updatedAt = now,
    )

    return customerRepository.save(customer).toDto()
  }

  @Transactional
  fun updateCustomer(
    managerId: String,
    agentId: Long?,
    customerId: String,
    customerPayload: CustomerPayload
  ): CustomerDto {
    val normalizedPayload = normalizePayload(customerPayload)

    validatePayload(normalizedPayload)
    agentId?.also { validateAgentExists(managerId, agentId) }
    validateUniquePhoneNumber(managerId, normalizedPayload.phoneNumber, excludeCustomerId = customerId)

    val currentCustomer = getCustomer(managerId, agentId, customerId)

    val updated = currentCustomer.copy(
      name = normalizedPayload.name,
      phoneNumber = normalizedPayload.phoneNumber,
      email = normalizedPayload.email,
      streetAddress = normalizedPayload.streetAddress,
      city = normalizedPayload.city,
      updatedAt = LocalDateTime.now(),
    )

    return customerRepository.save(updated).toDto()
  }

  @Transactional
  fun deleteCustomer(managerId: String, agentId: Long?, customerId: String) {
    val currentCustomer = getCustomer(managerId, agentId, customerId)

    val overrides = productOverrideRepository.findByManagerIdAndCustomerId(managerId, customerId)
    productOverrideRepository.deleteAll(overrides)

    customerRepository.delete(currentCustomer)
  }

  private fun validateUniquePhoneNumber(managerId: String, phoneNumber: String, excludeCustomerId: String?) {
    val customerWithSamePhoneNumber = customerRepository.findByManagerIdAndPhoneNumber(managerId, phoneNumber)
    if (customerWithSamePhoneNumber != null && customerWithSamePhoneNumber.id != excludeCustomerId) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = CustomerFailureReason.CUSTOMER_ALREADY_EXISTS.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_ALREADY_EXISTS.technical + "phoneNumber=$phoneNumber managerId=$managerId",
        severity = SeverityLevel.WARN,
      )
    }
  }

  private fun normalizePayload(customerPayload: CustomerPayload) =
    CustomerPayload(
      name = customerPayload.name.trim(),
      phoneNumber = customerPayload.phoneNumber.trim(),
      email = customerPayload.email.trim(),
      streetAddress = customerPayload.streetAddress.trim(),
      city = customerPayload.city.trim(),
    )

  private fun validatePayload(customerPayload: CustomerPayload) {
    FieldValidators.validateNonEmpty(customerPayload.name, "'name'")
    FieldValidators.validatePhoneNumber(customerPayload.phoneNumber)
    FieldValidators.validateEmail(customerPayload.email)
    FieldValidators.validateNonEmpty(customerPayload.streetAddress, "'street address'")
    FieldValidators.validateNonEmpty(customerPayload.city, "'city'")
  }

  private fun getCustomer(managerId: String, agentId: Long?, customerId: String): CustomerDbEntity {
    if (agentId == null) {
      return customerRepository.findByManagerIdAndId(managerId, customerId)
        ?: throw ServiceException(
          status = HttpStatus.NOT_FOUND,
          userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
          technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId customerId=$customerId",
          severity = SeverityLevel.WARN,
        )
    }

    validateAgentExists(managerId, agentId)

    return customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId agentId=$agentId customerId=$customerId",
        severity = SeverityLevel.WARN,
      )
  }

  private fun validateCustomerCap(managerId: String, agentId: Long?) {
    if (agentId != null) {
      val agentCustomerCount = customerRepository.countByAgentId(agentId)
      if (agentCustomerCount >= MAX_CUSTOMER_CAP) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = CustomerFailureReason.AGENT_CUSTOMER_LIMIT_EXCEEDED.userMessage,
          technicalMessage = CustomerFailureReason.AGENT_CUSTOMER_LIMIT_EXCEEDED.technical + "agentId=$agentId",
          severity = SeverityLevel.WARN,
        )
      }
    } else {
      val managerCustomerCount = customerRepository.countByManagerIdAndAgentIdIsNull(managerId)
      if (managerCustomerCount >= MAX_CUSTOMER_CAP) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = CustomerFailureReason.CUSTOMER_LIMIT_EXCEEDED.userMessage,
          technicalMessage = CustomerFailureReason.CUSTOMER_LIMIT_EXCEEDED.technical + "managerId=$managerId",
          severity = SeverityLevel.WARN,
        )
      }
    }
  }

  private fun validateAgentExists(managerId: String, agentId: Long) {
    val agent = agentRepository.findByManagerIdAndId(managerId, agentId)
    if (agent == null) {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.AGENT_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.AGENT_NOT_FOUND.technical + "managerId=$managerId agentId=$agentId",
        severity = SeverityLevel.WARN,
      )
    }
  }

}
