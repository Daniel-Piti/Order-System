package com.pt.ordersystem.ordersystem.productOverrides

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.productOverrides.models.ProductOverrideDto
import com.pt.ordersystem.ordersystem.productOverrides.models.UpdateProductOverrideRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Product overrides", description = "Product overrides API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/product-overrides")
@PreAuthorize(AUTH_USER)
class ProductOverrideController(
  private val productOverrideService: ProductOverrideService
) {

  @PostMapping
  fun createProductOverride(
    @RequestBody request: CreateProductOverrideRequest
  ): ResponseEntity<ProductOverrideDto> {
    val userId = AuthUtils.getCurrentUserId()
    val override = productOverrideService.createProductOverride(userId, request)
    return ResponseEntity.ok(override)
  }

  @GetMapping
  fun getProductOverrides(): ResponseEntity<List<ProductOverrideDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val overrides = productOverrideService.getProductOverridesByUserId(userId)
    return ResponseEntity.ok(overrides)
  }

  @GetMapping("/{overrideId}")
  fun getProductOverride(
    @PathVariable overrideId: String
  ): ResponseEntity<ProductOverrideDto> {
    val userId = AuthUtils.getCurrentUserId()
    val override = productOverrideService.getProductOverrideById(userId, overrideId)
    return ResponseEntity.ok(override)
  }

  @GetMapping("/product/{productId}")
  fun getProductOverridesByProduct(
    @PathVariable productId: String
  ): ResponseEntity<List<ProductOverrideDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val overrides = productOverrideService.getProductOverridesByProductId(userId, productId)
    return ResponseEntity.ok(overrides)
  }

  @PutMapping("/{overrideId}")
  fun updateProductOverride(
    @PathVariable overrideId: String,
    @RequestBody request: UpdateProductOverrideRequest
  ): ResponseEntity<ProductOverrideDto> {
    val userId = AuthUtils.getCurrentUserId()
    val override = productOverrideService.updateProductOverride(userId, overrideId, request)
    return ResponseEntity.ok(override)
  }

  @DeleteMapping("/{overrideId}")
  fun deleteProductOverride(
    @PathVariable overrideId: String
  ): ResponseEntity<Void> {
    val userId = AuthUtils.getCurrentUserId()
    productOverrideService.deleteProductOverride(userId, overrideId)
    return ResponseEntity.ok().build()
  }
}
