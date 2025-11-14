package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
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


@Tag(name = "Orders", description = "Order management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/orders")
@PreAuthorize(AUTH_MANAGER)
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
    @RequestParam(required = false) agentId: Long?
  ): ResponseEntity<Page<OrderDto>> {
    val orders = orderService.getOrders(
      managerId = manager.id,
      page = page,
      size = size,
      sortBy = sortBy,
      sortDirection = sortDirection,
      status = status,
      filterAgent = filterAgent,
      agentId = agentId
    )
    return ResponseEntity.ok(orders)
  }

  @GetMapping("/{orderId}")
  fun getOrderById(
    @PathVariable orderId: String,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<OrderDto> =
    ResponseEntity.ok(orderService.getOrderById(orderId = orderId, managerId = manager.id, agentId = null))

  @PostMapping
  fun createOrder(
    @RequestBody request: CreateOrderRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    // Manager creates order - agentId is null (comes from auth, not request body)
    val newOrderId = orderService.createOrder(managerId = manager.id, agentId = null, request = request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrderId)
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
  ): ResponseEntity<Void> {
    orderService.updateOrder(orderId = orderId, managerId = manager.id, agentId = null, request = request)
    return ResponseEntity.noContent().build()
  }

}
