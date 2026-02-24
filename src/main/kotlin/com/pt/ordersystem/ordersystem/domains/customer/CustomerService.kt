package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.agent.AgentRepository
import com.pt.ordersystem.ordersystem.domains.customer.models.Customer
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDto
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerFailureReason
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerPayload
import com.pt.ordersystem.ordersystem.domains.customer.models.toDto
import com.pt.ordersystem.ordersystem.domains.customer.models.toDbEntity
import com.pt.ordersystem.ordersystem.domains.customer.validators.CustomerValidators
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
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

  fun getCustomers(managerId: String, agentId: String?): List<Customer> {
    val customers = if (agentId == null) {
      customerRepository.findByManagerId(managerId)
    } else {
      agentRepository.findByManagerIdAndId(managerId, agentId)
      customerRepository.findByManagerIdAndAgentId(managerId, agentId)
    }
    return customers
  }

  fun getCustomerDto(managerId: String, customerId: String, agentId: String? = null) =
    getCustomer(managerId, agentId, customerId).toDto()

  @Transactional
  fun createCustomer(managerId: String, agentId: String?, customerPayload: CustomerPayload): CustomerDto {
    val normalizedPayload = normalizePayload(customerPayload)

    CustomerValidators.validatePayload(normalizedPayload)
    agentId?.also { agentRepository.findByManagerIdAndId(managerId, it) }
    validateUniquePhoneNumber(managerId, normalizedPayload.phoneNumber, excludeCustomerId = null)
    validateCustomerCap(managerId, agentId)

    val now = LocalDateTime.now()

    val entity = CustomerDbEntity(
      id = GeneralUtils.genId(),
      agentId = agentId,
      managerId = managerId,
      discountPercentage = normalizedPayload.discountPercentage,
      name = normalizedPayload.name,
      phoneNumber = normalizedPayload.phoneNumber,
      email = normalizedPayload.email,
      streetAddress = normalizedPayload.streetAddress,
      city = normalizedPayload.city,
      stateId = normalizedPayload.stateId,
      createdAt = now,
      updatedAt = now,
    )

    val customer = customerRepository.save(entity)
    return customer.toDto()
  }

  @Transactional
  fun updateCustomer(
    managerId: String,
    agentId: String?,
    customerId: String,
    customerPayload: CustomerPayload
  ): CustomerDto {
    val normalizedPayload = normalizePayload(customerPayload)

    CustomerValidators.validatePayload(normalizedPayload)
    agentId?.also { agentRepository.findByManagerIdAndId(managerId, it) }
    validateUniquePhoneNumber(managerId, normalizedPayload.phoneNumber, excludeCustomerId = customerId)

    val currentCustomer = getCustomer(managerId, agentId, customerId)

    val updatedEntity = currentCustomer.toDbEntity().copy(
      name = normalizedPayload.name,
      phoneNumber = normalizedPayload.phoneNumber,
      email = normalizedPayload.email,
      streetAddress = normalizedPayload.streetAddress,
      city = normalizedPayload.city,
      stateId = normalizedPayload.stateId,
      discountPercentage = normalizedPayload.discountPercentage,
      updatedAt = LocalDateTime.now(),
    )

    val customer = customerRepository.save(updatedEntity)
    return customer.toDto()
  }

  @Transactional
  fun deleteCustomer(managerId: String, agentId: String?, customerId: String) {
    getCustomer(managerId, agentId, customerId)

    val overrides = productOverrideRepository.findByManagerIdAndCustomerId(managerId, customerId)
    productOverrideRepository.deleteAll(overrides)

    customerRepository.deleteById(customerId)
  }

  @Transactional
  fun unassignCustomersFromAgent(managerId: String, agentId: String) {
    val customers = customerRepository.findByManagerIdAndAgentId(managerId, agentId)
    customers.forEach { customer ->
      val updatedEntity = customer.toDbEntity().copy(
        agentId = null,
        updatedAt = LocalDateTime.now()
      )
      customerRepository.save(updatedEntity)
    }
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
      discountPercentage = customerPayload.discountPercentage.coerceIn(0, 100),
      email = customerPayload.email.trim(),
      streetAddress = customerPayload.streetAddress.trim(),
      city = customerPayload.city.trim(),
      stateId = customerPayload.stateId.trim(),
    )

  private fun getCustomer(managerId: String, agentId: String?, customerId: String): Customer {
    if (agentId == null) {
      return customerRepository.findByManagerIdAndId(managerId, customerId)
    }

    agentRepository.findByManagerIdAndId(managerId, agentId)

    return customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
  }

  private fun validateCustomerCap(managerId: String, agentId: String?) {
    if (agentId != null) {
      val agentCustomerCount = customerRepository.countByAgentId(agentId)
      CustomerValidators.validateCustomersCap(agentCustomerCount, managerId, agentId)
    } else {
      val managerCustomerCount = customerRepository.countByManagerIdAndAgentIdIsNull(managerId)
      CustomerValidators.validateCustomersCap(managerCustomerCount, managerId, agentId)
    }
  }

}
