package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.order.models.CreateEmptyOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@Tag(name = "Orders", description = "Order management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/orders")
@PreAuthorize(AUTH_USER)
class OrderController(
  private val orderService: OrderService
) {

  @GetMapping
  fun getAllOrders(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<OrderDto>> {
    val orders = orderService.getAllOrdersForUser(user.userId)
    return ResponseEntity.ok(orders)
  }

  @GetMapping("/{orderId}")
  fun getOrderById(
    @PathVariable orderId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<OrderDto> =
    ResponseEntity.ok(orderService.getOrderById(orderId, user.userId))

  @PostMapping
  fun createEmptyOrder(
    @RequestBody request: CreateEmptyOrderRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val newOrderId = orderService.createEmptyOrder(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrderId)
  }

}