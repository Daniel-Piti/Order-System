package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.product.models.ProductDto
import com.pt.ordersystem.ordersystem.product.models.ProductRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@Tag(name = "Products", description = "Product management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/products")
class ProductController(
  private val productService: ProductService
) {

  @PreAuthorize(AUTH_USER)
  @GetMapping
  fun getAllMyProducts(): ResponseEntity<List<ProductDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val products = productService.getAllProductsForUser(userId)
    return ResponseEntity.ok(products)
  }

  @PreAuthorize(AUTH_USER)
  @GetMapping("/{productId}")
  fun getProduct(@PathVariable productId: String): ResponseEntity<ProductDto> {
    val product = productService.getProductById(productId)
    return ResponseEntity.ok(product)
  }

  @PreAuthorize(AUTH_USER)
  @PostMapping
  fun createProduct(@RequestBody request: ProductRequest): ResponseEntity<String> {
    val userId = AuthUtils.getCurrentUserId()
    val newProductId = productService.createProduct(userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newProductId)
  }

  @PreAuthorize(AUTH_USER)
  @PutMapping("/{productId}")
  fun updateProduct(
    @PathVariable productId: String,
    @RequestBody request: ProductRequest
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