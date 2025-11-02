package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderPublicDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Orders", description = "Public order API for customers")
@RestController
@RequestMapping("/api/public/orders")
class PublicOrderController(
  private val orderService: OrderService
) {

  @GetMapping("/{orderId}")
  fun getOrderById(@PathVariable orderId: String): ResponseEntity<OrderPublicDto> =
    ResponseEntity.ok(orderService.getOrderByIdPublic(orderId))

}
