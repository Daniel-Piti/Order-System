package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.*
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomerService(
  private val customerRepository: CustomerRepository
) {

  fun createCustomer(userId: String, request: CreateCustomerRequest): CustomerDto {
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
    
    customerRepository.delete(customer)
  }

}
