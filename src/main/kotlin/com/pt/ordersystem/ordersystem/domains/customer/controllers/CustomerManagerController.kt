package com.pt.ordersystem.ordersystem.domains.customer.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDto
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerPayload
import com.pt.ordersystem.ordersystem.domains.customer.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Manager Customers", description = "Managers manage their own customers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/manager/customers")
@PreAuthorize(AUTH_MANAGER)
class CustomerManagerController(
  private val customerService: CustomerService
) {

  @GetMapping("/{customerId}")
  fun getCustomerById(
    @PathVariable customerId: String,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<CustomerDto> {
    val customer = customerService.getCustomerDto(manager.id, customerId)
    return ResponseEntity.ok(customer)
  }

  @GetMapping
  fun getAllCustomers(@AuthenticationPrincipal manager: AuthUser): ResponseEntity<List<CustomerDto>> {
    val customers = customerService.getCustomers(manager.id, agentId = null)
    return ResponseEntity.ok(customers.map { it.toDto() })
  }

  @PostMapping
  fun createCustomer(
    @RequestBody payload: CustomerPayload,
    @AuthenticationPrincipal manager: AuthUser,
  ): ResponseEntity<CustomerDto> {
    val customer = customerService.createCustomer(manager.id, agentId = null, customerPayload = payload)
    return ResponseEntity.status(HttpStatus.CREATED).body(customer)
  }

  @PutMapping("/{customerId}")
  fun updateCustomer(
    @PathVariable customerId: String,
    @RequestBody payload: CustomerPayload,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<CustomerDto> {
    val existing = customerService.getCustomerDto(manager.id, customerId)
    val customer = customerService.updateCustomer(
      managerId = manager.id,
      agentId = existing.agentId,
      customerId = customerId,
      customerPayload = payload,
    )
    return ResponseEntity.ok(customer.toDto())
  }

  @DeleteMapping("/{customerId}")
  fun deleteCustomer(
    @PathVariable customerId: String,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    val existing = customerService.getCustomerDto(manager.id, customerId)
    customerService.deleteCustomer(manager.id, agentId = existing.agentId, customerId = customerId)
    return ResponseEntity.ok("Customer deleted successfully")
  }
}
