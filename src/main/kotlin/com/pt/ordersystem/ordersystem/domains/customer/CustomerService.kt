package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.*
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomerService(
  private val customerRepository: CustomerRepository,
  private val productOverrideRepository: ProductOverrideRepository
) {

  companion object {
    private const val MAX_CUSTOMERS_PER_USER = 1000
  }

  fun createCustomer(userId: String, request: CreateCustomerRequest): CustomerDto {
    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePhoneNumber(phoneNumber)
      FieldValidators.validateEmail(email)
    }

    // Check if user has reached the maximum number of customers
    val existingCustomersCount = customerRepository.findByUserId(userId).size
    if (existingCustomersCount >= MAX_CUSTOMERS_PER_USER) {
      throw ServiceException(
        status = org.springframework.http.HttpStatus.BAD_REQUEST,
        userMessage = CustomerFailureReason.CUSTOMER_LIMIT_EXCEEDED.name,
        technicalMessage = "User $userId has reached the maximum limit of $MAX_CUSTOMERS_PER_USER customers",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    }

    // Check if customer with same phone number already exists for this user
    val existingCustomer = customerRepository.findByUserIdAndPhoneNumber(userId, request.phoneNumber)
    if (existingCustomer != null) {
      throw ServiceException(
        status = org.springframework.http.HttpStatus.BAD_REQUEST,
        userMessage = CustomerFailureReason.CUSTOMER_ALREADY_EXISTS.name,
        technicalMessage = "Customer with phone number ${request.phoneNumber} already exists for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    }

    val customer = CustomerDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      name = request.name,
      phoneNumber = request.phoneNumber,
      email = request.email,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    val savedCustomer = customerRepository.save(customer)
    return savedCustomer.toDto()
  }

  fun getCustomersByUserId(userId: String): List<CustomerDto> {
    return customerRepository.findByUserId(userId).map { it.toDto() }
  }

  fun getCustomerByIdAndUserId(userId: String, customerId: String): CustomerDto {
    val customer = customerRepository.findByUserIdAndId(userId, customerId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.name,
        technicalMessage = "Customer with id $customerId not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    return customer.toDto()
  }

  fun updateCustomer(userId: String, customerId: String, request: UpdateCustomerRequest): CustomerDto {
    // Validate input fields
    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePhoneNumber(phoneNumber)
      FieldValidators.validateEmail(email)
    }

    val customer = customerRepository.findByUserIdAndId(userId, customerId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.name,
        technicalMessage = "Customer with id $customerId not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )

    val updatedCustomer = customer.copy(
      name = request.name,
      phoneNumber = request.phoneNumber,
      email = request.email,
      updatedAt = LocalDateTime.now()
    )

    val savedCustomer = customerRepository.save(updatedCustomer)
    return savedCustomer.toDto()
  }

  fun deleteCustomer(userId: String, customerId: String) {
    val customer = customerRepository.findByUserIdAndId(userId, customerId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.name,
        technicalMessage = "Customer with id $customerId not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    
    // Delete all product overrides for this customer first (cascading delete)
    val overrides = productOverrideRepository.findByUserIdAndCustomerId(userId, customerId)
    productOverrideRepository.deleteAll(overrides)
    
    customerRepository.delete(customer)
  }

}
