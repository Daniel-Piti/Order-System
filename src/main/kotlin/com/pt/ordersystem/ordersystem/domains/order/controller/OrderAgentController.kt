package com.pt.ordersystem.ordersystem.domains.order.controller

import com.pt.ordersystem.ordersystem.auth.AuthRole
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.order.OrderListFilters
import com.pt.ordersystem.ordersystem.domains.order.OrderListFiltersExternal
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.order.models.CreateOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderSource
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateDiscountRequest
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.toDto
import com.pt.ordersystem.ordersystem.utils.PageRequestBaseExternal
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Agent Orders", description = "Agent order management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/agent/orders")
@PreAuthorize(AuthRole.AUTH_AGENT)
class OrderAgentController(
    private val orderService: OrderService,
    private val agentService: AgentService,
) {

  @GetMapping
  fun getAllOrders(
      @AuthenticationPrincipal agentAuthUser: AuthUser,
      filters: OrderListFiltersExternal,
      pageParams: PageRequestBaseExternal,
  ): ResponseEntity<Page<OrderDto>> {
    val agent = agentService.getAgent(agentAuthUser.id)
    val filters = OrderListFilters(
      managerId = agent.managerId,
      orderSource = OrderSource.AGENT,
      agentId = agent.id,
      status = filters.status,
      customerId = filters.customerId,
    )
    val orders = orderService.getOrders(
      managerId = agent.managerId,
      filters = filters,
      pageRequestBase = pageParams.toPageRequestBase(),
    )
    return ResponseEntity.ok(orders.map { it.toDto() })
  }

  @GetMapping("/{orderId}")
  fun getOrderById(
      @PathVariable orderId: String,
      @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<OrderDto> {
    val agent = agentService.getAgent(agent.id)
    val order = orderService.getOrderById(orderId = orderId, managerId = agent.managerId, agentId = agent.id)
    return ResponseEntity.ok(order.toDto())
  }

  @PostMapping
  fun createOrder(
      @RequestBody request: CreateOrderRequest,
      @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<OrderDto> {
    val loadedAgent = agentService.getAgent(agent.id)
    val newOrder = orderService.createOrder(
      managerId = loadedAgent.managerId,
      agentId = loadedAgent.id,
      orderSource = OrderSource.AGENT,
      request = request,
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrder.toDto())
  }

  @PutMapping("/{orderId}/status/cancelled")
  fun cancelOrder(
      @PathVariable orderId: String,
      @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<OrderDto> {
    val loadedAgent = agentService.getAgent(agent.id)
    val canceledOrder = orderService.cancelOrder(orderId, loadedAgent.managerId, loadedAgent.id)
    return ResponseEntity.status(HttpStatus.OK).body(canceledOrder.toDto())
  }

  @PutMapping("/{orderId}")
  fun updateOrder(
      @PathVariable orderId: String,
      @RequestBody request: UpdateOrderRequest,
      @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<OrderDto> {
    val loadedAgent = agentService.getAgent(agent.id)
    val updatedOrder = orderService.updateOrder(
      orderId = orderId,
      managerId = loadedAgent.managerId,
      agentId = loadedAgent.id,
      updateOrderRequest = request
    )
    return ResponseEntity.status(HttpStatus.OK).body(updatedOrder.toDto())
  }

  @PutMapping("/{orderId}/discount")
  fun updateOrderDiscount(
      @PathVariable orderId: String,
      @RequestBody request: UpdateDiscountRequest,
      @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<OrderDto> {
    val loadedAgent = agentService.getAgent(agent.id)
    val updatedOrder = orderService.updateOrderDiscount(
      orderId = orderId,
      managerId = loadedAgent.managerId,
      agentId = loadedAgent.id,
      request = request
    )
    return ResponseEntity.status(HttpStatus.OK).body(updatedOrder.toDto())
  }

}