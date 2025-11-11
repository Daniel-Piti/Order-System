package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideWithPriceDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.UpdateProductOverrideRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Product overrides", description = "Product overrides API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/product-overrides")
@PreAuthorize(AUTH_MANAGER)
class ProductOverrideController(
  private val productOverrideService: ProductOverrideService
) {

  @GetMapping
  fun getAllOverrides(
    @AuthenticationPrincipal user: AuthUser,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "customer_id") sortBy: String,
    @RequestParam(defaultValue = "ASC") sortDirection: String,
    @RequestParam(required = false) productId: String?,
    @RequestParam(required = false) customerId: String?
  ): ResponseEntity<Page<ProductOverrideWithPriceDto>> {
    val overrides = productOverrideService.getAllOverrides(
      userId = user.id,
      page = page,
      size = size,
      sortBy = sortBy,
      sortDirection = sortDirection,
      productId = productId,
      customerId = customerId
    )
    return ResponseEntity.ok(overrides)
  }

  @GetMapping("/override/{overrideId}")
  fun getOverrideById(
    @PathVariable overrideId: Long,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    val override = productOverrideService.getProductOverrideById(user.id, overrideId)
    return ResponseEntity.ok(override)
  }

  @GetMapping("/product/{productId}")
  fun getOverridesByProduct(
    @PathVariable productId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<List<ProductOverrideDto>> {
    val overrides = productOverrideService.getProductOverridesForProductId(user.id, productId)
    return ResponseEntity.ok(overrides)
  }

  @PostMapping
  fun createOverride(
    @RequestBody request: CreateProductOverrideRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    val override = productOverrideService.createProductOverride(user.id, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(override)
  }

  @PutMapping("/override/{overrideId}")
  fun updateOverride(
    @PathVariable overrideId: Long,
    @RequestBody request: UpdateProductOverrideRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    val override = productOverrideService.updateProductOverride(user.id, overrideId, request)
    return ResponseEntity.ok(override)
  }

  @DeleteMapping("/override/{overrideId}")
  fun deleteOverride(
    @PathVariable overrideId: Long,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    productOverrideService.deleteProductOverride(user.id, overrideId)
    return ResponseEntity.ok("Product override deleted successfully")
  }

}
