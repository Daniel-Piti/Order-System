package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_AGENT
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.order.models.CreateOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateOrderRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Agent Orders", description = "Agent order management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/agent/orders")
@PreAuthorize(AUTH_AGENT)
class OrderAgentController(
  private val orderService: OrderService,
  private val agentService: AgentService,
) {

  @GetMapping
  fun getAllOrders(
    @AuthenticationPrincipal agent: AuthUser,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "createdAt") sortBy: String,
    @RequestParam(defaultValue = "DESC") sortDirection: String,
    @RequestParam(required = false) status: String?
  ): ResponseEntity<Page<OrderDto>> {
    val agentEntity = agentService.getAgentEntity(agent.id)
    val orders = orderService.getOrders(
      managerId = agentEntity.managerId,
      page = page,
      size = size,
      sortBy = sortBy,
      sortDirection = sortDirection,
      status = status,
      filterAgent = true,
      agentId = agentEntity.id
    )
    return ResponseEntity.ok(orders)
  }

  @GetMapping("/{orderId}")
  fun getOrderById(
    @PathVariable orderId: String,
    @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<OrderDto> {
    val agentEntity = agentService.getAgentEntity(agent.id)
    return ResponseEntity.ok(
      orderService.getOrderById(orderId = orderId, managerId = agentEntity.managerId, agentId = agentEntity.id)
    )
  }

  @PostMapping
  fun createOrder(
    @RequestBody request: CreateOrderRequest,
    @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<String> {
    val agentEntity = agentService.getAgentEntity(agent.id)
    val newOrderId = orderService.createOrder(
      managerId = agentEntity.managerId,
      agentId = agentEntity.id,
      request = request
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrderId)
  }

  @PutMapping("/{orderId}/status/cancelled")
  fun cancelOrder(
    @PathVariable orderId: String,
    @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<Void> {
    val agentEntity = agentService.getAgentEntity(agent.id)
    orderService.cancelOrder(orderId, agentEntity.managerId, agentEntity.id)
    return ResponseEntity.noContent().build()
  }

  @PutMapping("/{orderId}")
  fun updateOrder(
    @PathVariable orderId: String,
    @RequestBody request: UpdateOrderRequest,
    @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<Void> {
    val agentEntity = agentService.getAgentEntity(agent.id)
    orderService.updateOrder(
      orderId = orderId,
      managerId = agentEntity.managerId,
      agentId = agentEntity.id,
      request = request
    )
    return ResponseEntity.noContent().build()
  }

}

