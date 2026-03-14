package com.pt.ordersystem.ordersystem.domains.productOverrides.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideWithPriceDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.UpdateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.toDto
import com.pt.ordersystem.ordersystem.utils.PageRequestBaseExternal
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Manager Product overrides", description = "Product overrides API for managers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/product-overrides")
@PreAuthorize(AUTH_MANAGER)
class ProductOverrideManagerController(
  private val productOverrideService: ProductOverrideService,
) {

  @GetMapping
  fun getAllOverrides(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam(required = false) productId: String?,
    pageParams: PageRequestBaseExternal,
  ): ResponseEntity<Page<ProductOverrideWithPriceDto>> {
    val overrides = productOverrideService.getOverrides(
      managerId = manager.id,
      agentId = null,
      pageRequestBase = pageParams.toPageRequestBase(),
      productId = productId,
    )
    return ResponseEntity.ok(overrides.map { it.toDto() })
  }

  @PostMapping
  fun createOverride(
    @RequestBody request: CreateProductOverrideRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    request.validate()

    val override = productOverrideService.createProductOverride(manager.id, null, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(override.toDto())
  }

  @PutMapping("/override/{overrideId}")
  fun updateOverride(
    @PathVariable overrideId: Long,
    @RequestBody request: UpdateProductOverrideRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<ProductOverrideDto> {
    request.validate()
    val override = productOverrideService.updateProductOverride(manager.id, null, overrideId, request)
    return ResponseEntity.ok(override.toDto())
  }

  @DeleteMapping("/override/{overrideId}")
  fun deleteOverride(
    @PathVariable overrideId: Long,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    productOverrideService.deleteProductOverride(
      managerId = manager.id,
      agentId = null,
      overrideId = overrideId
    )
    return ResponseEntity.ok("Product override deleted successfully")
  }
}
