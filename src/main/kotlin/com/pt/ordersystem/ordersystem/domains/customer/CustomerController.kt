package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.customer.models.CreateCustomerRequest
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDto
import com.pt.ordersystem.ordersystem.domains.customer.models.UpdateCustomerRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Customers", description = "Customers management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/customers")
@PreAuthorize(AUTH_USER)
class CustomerController(
  private val customerService: CustomerService
) {

  @GetMapping("/{customerId}")
  fun getCustomerById(
    @PathVariable customerId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<CustomerDto> {
    val customer = customerService.getCustomerByIdAndUserId(user.userId, customerId)
    return ResponseEntity.ok(customer)
  }

  @GetMapping
  fun getAllCustomers(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<CustomerDto>> {
    val customers = customerService.getCustomersByUserId(user.userId)
    return ResponseEntity.ok(customers)
  }

  @PostMapping
  fun createCustomer(
    @RequestBody request: CreateCustomerRequest,
    @AuthenticationPrincipal user: AuthUser,
  ): ResponseEntity<CustomerDto> {
    val customer = customerService.createCustomer(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(customer)
  }

  @PutMapping("/{customerId}")
  fun updateCustomer(
    @PathVariable customerId: String,
    @RequestBody request: UpdateCustomerRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<CustomerDto> {
    val customer = customerService.updateCustomer(user.userId, customerId, request)
    return ResponseEntity.ok(customer)
  }

  @DeleteMapping("/{customerId}")
  fun deleteCustomer(
    @PathVariable customerId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    customerService.deleteCustomer(user.userId, customerId)
    return ResponseEntity.ok("Customer deleted successfully")
  }
}
