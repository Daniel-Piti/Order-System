package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.UpdateProductOverrideRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Product overrides", description = "Product overrides API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/product-overrides")
@PreAuthorize(AUTH_USER)
class ProductOverrideController(
  private val productOverrideService: ProductOverrideService
) {

  @GetMapping
  fun getAllOverrides(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<ProductOverrideDto>> {
    val overrides = productOverrideService.getProductOverridesByUserId(user.userId)
    return ResponseEntity.ok(overrides)
  }

  @GetMapping("/override/{overrideId}")
  fun getOverrideById(
    @PathVariable overrideId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    val override = productOverrideService.getProductOverrideById(user.userId, overrideId)
    return ResponseEntity.ok(override)
  }

  @GetMapping("/product/{productId}")
  fun getOverridesByProduct(
    @PathVariable productId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<List<ProductOverrideDto>> {
    val overrides = productOverrideService.getProductOverridesForProductId(user.userId, productId)
    return ResponseEntity.ok(overrides)
  }

  @PostMapping
  fun createOverride(
    @RequestBody request: CreateProductOverrideRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    val override = productOverrideService.createProductOverride(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(override)
  }

  @PutMapping("/override/{overrideId}")
  fun updateOverride(
    @PathVariable overrideId: String,
    @RequestBody request: UpdateProductOverrideRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    val override = productOverrideService.updateProductOverride(user.userId, overrideId, request)
    return ResponseEntity.ok(override)
  }

  @DeleteMapping("/override/{overrideId}")
  fun deleteOverride(
    @PathVariable overrideId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    productOverrideService.deleteProductOverride(user.userId, overrideId)
    return ResponseEntity.ok("Product override deleted successfully")
  }

}
