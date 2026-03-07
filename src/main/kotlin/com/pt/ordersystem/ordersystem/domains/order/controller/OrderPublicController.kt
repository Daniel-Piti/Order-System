package com.pt.ordersystem.ordersystem.domains.order.controller

import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.order.models.OrderPublicDto
import com.pt.ordersystem.ordersystem.domains.order.models.PlaceOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.toPublicDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Public Orders", description = "Public order API for customers")
@RestController
@RequestMapping("/api/public/orders")
class OrderPublicController(
  private val orderService: OrderService
) {

  @GetMapping("/{orderId}")
  fun getOrderById(@PathVariable orderId: String): ResponseEntity<OrderPublicDto> =
    ResponseEntity.ok(orderService.getOrderById(orderId).toPublicDto())

  @PutMapping("/{orderId}/place")
  fun placeOrder(
      @PathVariable orderId: String,
      @RequestBody request: PlaceOrderRequest
  ): ResponseEntity<String> {
    orderService.placeOrder(orderId, request)
    return ResponseEntity.ok("Order placed successfully")
  }

  @PostMapping("/manager/{managerId}/create")
  fun createAndPlacePublicOrder(
      @PathVariable managerId: String,
      @RequestBody request: PlaceOrderRequest
  ): ResponseEntity<String> {
    val orderId = orderService.createAndPlacePublicOrder(managerId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(orderId)
  }

}