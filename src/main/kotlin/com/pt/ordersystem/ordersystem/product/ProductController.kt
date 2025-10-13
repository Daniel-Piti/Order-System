package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.product.models.*
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Products", description = "Product management API")
@RestController
@RequestMapping("/api/products")
class ProductController(
  private val productService: ProductService
) {

  // ----------------------
  // Public endpoints
  // ----------------------

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

  @GetMapping("/product/{productId}")
  fun getProduct(@PathVariable productId: String): ResponseEntity<ProductDto> {
    val product = productService.getProductById(productId)
    return ResponseEntity.ok(product)
  }

  // ----------------------
  // Authenticated endpoints (only for the logged-in user)
  // ----------------------

  @PreAuthorize(AUTH_USER)
  @GetMapping("/me")
  fun getAllMyProducts(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<ProductDto>> {
    val products = productService.getAllProductsForUser(user.userId)
    return ResponseEntity.ok(products)
  }

  @PreAuthorize(AUTH_USER)
  @PostMapping
  fun createProduct(
    @RequestBody request: CreateProductRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val newProductId = productService.createProduct(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newProductId)
  }

  @PreAuthorize(AUTH_USER)
  @PutMapping("/{productId}")
  fun updateProduct(
    @PathVariable productId: String,
    @RequestBody request: UpdateProductRequest
  ): ResponseEntity<String> {
    val updatedProductId = productService.updateProduct(productId, request)
    return ResponseEntity.ok(updatedProductId)
  }

  @PreAuthorize(AUTH_USER)
  @DeleteMapping("/{productId}")
  fun deleteProduct(@PathVariable productId: String): ResponseEntity<String> {
    productService.deleteProduct(productId)
    return ResponseEntity.ok("Product deleted successfully")
  }
}
