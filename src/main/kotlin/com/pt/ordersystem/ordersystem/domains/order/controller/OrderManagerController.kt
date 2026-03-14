package com.pt.ordersystem.ordersystem.domains.order.controller

import com.pt.ordersystem.ordersystem.auth.AuthRole
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.order.models.CreateOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderSource
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateDiscountRequest
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.toDto
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Orders", description = "Order management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/orders")
@PreAuthorize(AuthRole.AUTH_MANAGER)
class OrderManagerController(
  private val orderService: OrderService
) {

  @GetMapping
  fun getAllOrders(
      @AuthenticationPrincipal manager: AuthUser,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "20") size: Int,
      @RequestParam(defaultValue = "createdAt") sortBy: String,
      @RequestParam(defaultValue = "DESC") sortDirection: String,
      @RequestParam(required = false) status: String?,
      @RequestParam(defaultValue = "false") filterAgent: Boolean,
      @RequestParam(required = false) agentId: String?,
      @RequestParam(required = false) customerId: String?,
  ): ResponseEntity<Page<OrderDto>> {
    val orders = if (customerId != null) {
      orderService.getOrdersByCustomerId(
        managerId = manager.id,
        customerId = customerId,
        page = page,
        pageSize = size,
        sortBy = sortBy,
        sortDirection = sortDirection,
        status = status,
        agentId = null
      )
    } else {
      orderService.getOrders(
        managerId = manager.id,
        page = page,
        pageSize = size,
        sortBy = sortBy,
        sortDirection = sortDirection,
        status = status,
        filterAgent = filterAgent,
        agentId = agentId
      )
    }
    return ResponseEntity.ok(orders.map { it.toDto() })
  }

  @GetMapping("/{orderId}")
  fun getOrderById(
      @PathVariable orderId: String,
      @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<OrderDto> {
    val order = orderService.getOrderById(orderId = orderId, managerId = manager.id, agentId = null)
    return ResponseEntity.ok(order.toDto())
  }

  @PostMapping
  fun createOrder(
      @RequestBody request: CreateOrderRequest,
      @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<OrderDto> {
    val newOrder = orderService.createOrder(
      managerId = manager.id,
      agentId = null,
      orderSource = OrderSource.MANAGER,
      request = request
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrder.toDto())
  }

  @PutMapping("/{orderId}/status/done")
  fun markOrderDone(
      @PathVariable orderId: String,
      @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<Void> {
    orderService.markOrderDone(orderId, manager.id)
    return ResponseEntity.noContent().build()
  }

  @PutMapping("/{orderId}/status/cancelled")
  fun cancelOrder(
      @PathVariable orderId: String,
      @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<Void> {
    orderService.cancelOrder(orderId, manager.id)
    return ResponseEntity.noContent().build()
  }

  @PutMapping("/{orderId}")
  fun updateOrder(
      @PathVariable orderId: String,
      @RequestBody request: UpdateOrderRequest,
      @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<OrderDto> {
    val updatedOrder = orderService.updateOrder(
        orderId = orderId,
        managerId = manager.id,
        agentId = null,
        updateOrderRequest = request
    )
    return ResponseEntity.status(HttpStatus.OK).body(updatedOrder.toDto())
  }

  @PutMapping("/{orderId}/discount")
  fun updateOrderDiscount(
      @PathVariable orderId: String,
      @RequestBody request: UpdateDiscountRequest,
      @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<OrderDto> {
    val updatedOrder = orderService.updateOrderDiscount(
        orderId = orderId,
        managerId = manager.id,
        agentId = null,
        request = request
    )
    return ResponseEntity.status(HttpStatus.OK).body(updatedOrder.toDto())
  }

}