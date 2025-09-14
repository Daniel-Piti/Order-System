package com.pt.ordersystem.ordersystem.order

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.order.models.CreateOrderRequest
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
@RequestMapping("/api/orders")
@PreAuthorize(AUTH_USER)
class OrderController(
  private val orderService: OrderService
) {

  @GetMapping
  fun getAllMyOrders(): ResponseEntity<List<OrderDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val orders = orderService.getAllOrdersForUser(userId)
    return ResponseEntity.ok(orders)
  }

  @GetMapping("/{orderId}")
  fun getOrder(@PathVariable orderId: String): ResponseEntity<OrderDto> =
    ResponseEntity.ok(orderService.getOrderById(orderId))

  @PostMapping
  fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<String> {
    val userId = AuthUtils.getCurrentUserId()
    val newOrderId = orderService.createOrder(userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newOrderId)
  }

  @DeleteMapping("/{orderId}")
  fun deleteOrder(@PathVariable orderId: String): ResponseEntity<String> {
    orderService.deleteOrder(orderId)
    return ResponseEntity.ok("Order deleted successfully")
  }
}