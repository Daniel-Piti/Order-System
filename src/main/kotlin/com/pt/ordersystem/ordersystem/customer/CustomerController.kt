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
    val customer = customerService.createCustomer(userId, request)
    return ResponseEntity.ok(customer)
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
    val customer = customerService.getCustomerById(userId, customerId)
    return ResponseEntity.ok(customer)
  }

  @PutMapping("/{customerId}")
  fun updateCustomer(
    @PathVariable customerId: String,
    @RequestBody request: UpdateCustomerRequest
  ): ResponseEntity<CustomerDto> {
    val userId = AuthUtils.getCurrentUserId()
    val customer = customerService.updateCustomer(userId, customerId, request)
    return ResponseEntity.ok(customer)
  }

  @DeleteMapping("/{customerId}")
  fun deleteCustomer(
    @PathVariable customerId: String
  ): ResponseEntity<Void> {
    val userId = AuthUtils.getCurrentUserId()
    customerService.deleteCustomer(userId, customerId)
    return ResponseEntity.ok().build()
  }
}
