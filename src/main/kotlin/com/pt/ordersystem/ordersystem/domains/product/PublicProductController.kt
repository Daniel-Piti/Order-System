package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Products", description = "Public product API for customers")
@RestController
@RequestMapping("/api/public/products")
class PublicProductController(
  private val productService: ProductService
) {

  @GetMapping("/user/{userId}/product/{productId}")
  fun getProduct(
    @PathVariable userId: String,
    @PathVariable productId: String
  ): ResponseEntity<ProductDto> {
    val product = productService.getProductById(productId)
    return ResponseEntity.ok(product)
  }

  @GetMapping("/user/{userId}")
  fun getAllUserProducts(@PathVariable userId: String): ResponseEntity<List<ProductDto>> {
    val products = productService.getAllProductsForUser(userId)
    return ResponseEntity.ok(products)
  }

  @GetMapping("/order/{orderId}")
  fun getAllProductsForOrder(@PathVariable orderId: String): ResponseEntity<List<ProductDto>> {
    val products = productService.getAllProductsForOrder(orderId)
    return ResponseEntity.ok(products)
  }

}

