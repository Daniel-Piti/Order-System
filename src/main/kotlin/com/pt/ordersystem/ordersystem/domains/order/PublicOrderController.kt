package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderPublicDto
import com.pt.ordersystem.ordersystem.domains.order.models.PlaceOrderRequest
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Orders", description = "Public order API for customers")
@RestController
@RequestMapping("/api/public/orders")
class PublicOrderController(
  private val orderService: OrderService
) {

  @GetMapping("/{orderId}")
  fun getOrderById(@PathVariable orderId: Long): ResponseEntity<OrderPublicDto> =
    ResponseEntity.ok(orderService.getOrderByIdPublic(orderId))

  @PutMapping("/{orderId}/place")
  fun placeOrder(
    @PathVariable orderId: Long,
    @RequestBody request: PlaceOrderRequest
  ): ResponseEntity<String> {
    orderService.placeOrder(orderId, request)
    return ResponseEntity.ok("Order placed successfully")
  }

  @PostMapping("/manager/{managerId}/create")
  fun createAndPlacePublicOrder(
    @PathVariable managerId: String,
    @RequestBody request: PlaceOrderRequest
  ): ResponseEntity<Long> {
    val orderId = orderService.createAndPlacePublicOrder(managerId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(orderId)
  }

}
