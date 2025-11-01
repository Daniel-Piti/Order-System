package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.product.models.CreateProductRequest
import com.pt.ordersystem.ordersystem.domains.product.models.UpdateProductRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Products", description = "Product management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products")
@PreAuthorize(AUTH_USER)
class ProductController(
  private val productService: ProductService
) {

  @PostMapping
  fun createProduct(
    @RequestBody request: CreateProductRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val newProductId = productService.createProduct(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(newProductId)
  }

  @PutMapping("/{productId}")
  fun updateProduct(
    @PathVariable productId: String,
    @RequestBody request: UpdateProductRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val updatedProductId = productService.updateProduct(productId, request)
    return ResponseEntity.ok(updatedProductId)
  }

  @DeleteMapping("/{productId}")
  fun deleteProduct(
    @PathVariable productId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    productService.deleteProduct(productId)
    return ResponseEntity.ok("Product deleted successfully")
  }

}
