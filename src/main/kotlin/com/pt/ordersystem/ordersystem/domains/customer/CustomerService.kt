package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.Customer
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerPayload
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CustomerService(
  private val customerRepository: CustomerRepository,
  private val customerValidationService: CustomerValidationService,
  private val productOverrideRepository: ProductOverrideRepository,
) {

  fun getCustomersForManager(managerId: String): List<Customer> =
    customerRepository.findByManagerId(managerId)

  fun findCustomerForManager(managerId: String, customerId: String): Customer =
    customerRepository.findByManagerIdAndId(managerId, customerId)

  fun getCustomersForAgent(agentId: String) =
    customerRepository.findByAgentId(agentId)

  @Transactional
  fun createCustomer(managerId: String, agentId: String?, customerPayload: CustomerPayload): Customer {
    customerValidationService.validateCustomerCap(managerId, agentId)

    val now = LocalDateTime.now()

    val customerDbEntity = CustomerDbEntity(
      id = GeneralUtils.genId(),
      agentId = agentId,
      managerId = managerId,
      discountPercentage = customerPayload.discountPercentage,
      name = customerPayload.name,
      phoneNumber = customerPayload.phoneNumber,
      email = customerPayload.email,
      streetAddress = customerPayload.streetAddress,
      city = customerPayload.city,
      stateId = customerPayload.stateId,
      createdAt = now,
      updatedAt = now,
    )

    return customerRepository.save(customerDbEntity)
  }

  @Transactional
  fun updateCustomer(
    managerId: String,
    agentId: String?,
    customerId: String,
    customerPayload: CustomerPayload
  ): Customer {

    val storedCustomer = customerRepository.findEntityByManagerIdAndAgentIdAndId(managerId, agentId, customerId)

    val updatedEntity = storedCustomer.copy(
      name = customerPayload.name,
      phoneNumber = customerPayload.phoneNumber,
      email = customerPayload.email,
      streetAddress = customerPayload.streetAddress,
      city = customerPayload.city,
      stateId = customerPayload.stateId,
      discountPercentage = customerPayload.discountPercentage,
      updatedAt = LocalDateTime.now(),
    )

    return customerRepository.save(updatedEntity)
  }

  @Transactional
  fun deleteCustomer(managerId: String, agentId: String?, customerId: String) {
    // validate customer ownership (agent/manager)
    customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)

    val overrides = productOverrideRepository.getAllForManagerIdAndCustomerId(managerId, customerId)
    productOverrideRepository.deleteAll(overrides)

    customerRepository.deleteById(customerId)
  }

  @Transactional
  fun deleteAllCustomersForAgent(managerId: String, agentId: String) {
    customerRepository.deleteByManagerIdAndAgentId(managerId, agentId)
  }

}
