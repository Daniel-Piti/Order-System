package com.pt.ordersystem.ordersystem.domains.customer.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_AGENT
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Agent Customers", description = "Agents manage their own customers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/agent/customers")
@PreAuthorize(AUTH_AGENT)
class CustomerAgentController(
    private val agentService: AgentService,
    private val customerService: CustomerService,
) {

  @GetMapping
  fun getCustomersForAgent(
    @AuthenticationPrincipal agent: AuthUser,
  ): ResponseEntity<List<CustomerDto>> {
    val agent = agentService.getAgent(agent.id)
    val customers = customerService.getCustomersForAgent(agent.id)
    return ResponseEntity.ok(customers.map { it.toDto() })
  }

  @PostMapping
  fun createCustomer(
    @AuthenticationPrincipal agent: AuthUser,
    @RequestBody payload: CustomerPayload,
  ): ResponseEntity<CustomerDto> {
    val normalPayload = payload.normalize()
    val agent = agentService.getAgent(agent.id)
    val customer = customerService.createCustomer(
      managerId = agent.managerId,
      agentId = agent.id,
      customerPayload = normalPayload
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(customer.toDto())
  }

  @PutMapping("/{customerId}")
  fun updateCustomer(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable customerId: String,
    @RequestBody payload: CustomerPayload,
  ): ResponseEntity<CustomerDto> {
    val normalizedPayload = payload.normalize()
    val agent = agentService.getAgent(agent.id)
    val updated = customerService.updateCustomer(
      managerId = agent.managerId,
      agentId = agent.id,
      customerId = customerId,
      customerPayload = normalizedPayload,
    )
    return ResponseEntity.ok(updated.toDto())
  }

  @DeleteMapping("/{customerId}")
  fun deleteCustomer(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable customerId: String,
  ): ResponseEntity<Void> {
    val agent = agentService.getAgent(agent.id)
    customerService.deleteCustomer(
      managerId = agent.managerId,
      agentId = agent.id,
      customerId = customerId,
    )
    return ResponseEntity.noContent().build()
  }
}

