package com.pt.ordersystem.ordersystem.order

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.order.models.EmptyOrderRequest
import com.pt.ordersystem.ordersystem.order.models.OrderDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@Tag(name = "Orders", description = "Order management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/orders")
class OrderController(
  private val orderService: OrderService
) {

  @PreAuthorize(AUTH_USER)
  @GetMapping
  fun getAllMyOrders(): ResponseEntity<List<OrderDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val orders = orderService.getAllOrdersForUser(userId)
    return ResponseEntity.ok(orders)
  }

  @PreAuthorize(AUTH_USER)
  @GetMapping("/{orderId}")
  fun getOrder(@PathVariable orderId: String): ResponseEntity<OrderDto> =
    ResponseEntity.ok(orderService.getOrderById(orderId))

  @PreAuthorize(AUTH_USER)
  @PostMapping
  fun createEmptyOrder(@RequestBody request: EmptyOrderRequest): ResponseEntity<String> {
    val userId = AuthUtils.getCurrentUserId()
    val newOrderId = orderService.createEmptyOrder(userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrderId)
  }

  @PreAuthorize(AUTH_USER)
  @DeleteMapping("/{orderId}")
  fun deleteOrder(@PathVariable orderId: String): ResponseEntity<String> {
    orderService.deleteOrder(orderId)
    return ResponseEntity.ok("Order deleted successfully")
  }
}