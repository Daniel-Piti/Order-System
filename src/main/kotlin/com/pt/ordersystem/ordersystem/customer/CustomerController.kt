package com.pt.ordersystem.ordersystem.customer

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.customer.models.*
import com.pt.ordersystem.ordersystem.exception.ServiceException
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Customers", description = "Customers management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/customers")
@PreAuthorize(AUTH_USER)
class CustomerController(
  private val customerService: CustomerService
) {

  @PostMapping
  fun createCustomer(
    @RequestBody request: CreateCustomerRequest
  ): ResponseEntity<CustomerDto> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      val customer = customerService.createCustomer(userId, request)
      ResponseEntity.ok(customer)
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }
  }

  @GetMapping
  fun getCustomers(): ResponseEntity<List<CustomerDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val customers = customerService.getCustomersByUserId(userId)
    return ResponseEntity.ok(customers)
  }

  @GetMapping("/{customerId}")
  fun getCustomer(
    @PathVariable customerId: String
  ): ResponseEntity<CustomerDto> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      val customer = customerService.getCustomerById(userId, customerId)
      ResponseEntity.ok(customer)
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @PutMapping("/{customerId}")
  fun updateCustomer(
    @PathVariable customerId: String,
    @RequestBody request: UpdateCustomerRequest
  ): ResponseEntity<CustomerDto> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      val customer = customerService.updateCustomer(userId, customerId, request)
      ResponseEntity.ok(customer)
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @DeleteMapping("/{customerId}")
  fun deleteCustomer(
    @PathVariable customerId: String
  ): ResponseEntity<Void> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      customerService.deleteCustomer(userId, customerId)
      ResponseEntity.ok().build()
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }
}
