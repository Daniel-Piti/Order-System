package com.pt.ordersystem.ordersystem.domains.product.controllers

import com.pt.ordersystem.ordersystem.domains.product.models.ProductPublicDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Products", description = "Public product API for customers")
@RestController
@RequestMapping("/api/public/products")
class ProductPublicController(
  private val productService: ProductService,
  private val managerService: ManagerService,
) {

  @GetMapping("/manager/{managerId}")
  fun getAllManagerProducts(@PathVariable managerId: String): ResponseEntity<List<ProductPublicDto>> {
    managerService.validateManagerExists(managerId)
    val products = productService.getAllProductsForManager(managerId)
    return ResponseEntity.ok(products.map { it.toPublicDto() })
  }

  @GetMapping("/order/{orderId}")
  fun getAllProductsForOrder(@PathVariable orderId: String): ResponseEntity<List<ProductPublicDto>> {
    val products = productService.getAllProductsForOrder(orderId)
    return ResponseEntity.ok(products.map { it.toPublicDto() })
  }
}

