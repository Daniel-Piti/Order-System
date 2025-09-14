package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.product.models.*
import com.pt.ordersystem.ordersystem.exception.ServiceException
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
class ProductOverrideController(
  private val productOverrideService: ProductOverrideService
) {

  @PreAuthorize(AUTH_USER)
  @PostMapping
  fun createProductOverride(
    @RequestBody request: CreateProductOverrideRequest
  ): ResponseEntity<ProductOverrideDto> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      val override = productOverrideService.createProductOverride(userId, request)
      ResponseEntity.ok(override)
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }
  }

  @PreAuthorize(AUTH_USER)
  @GetMapping
  fun getProductOverrides(): ResponseEntity<List<ProductOverrideDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val overrides = productOverrideService.getProductOverridesByUserId(userId)
    return ResponseEntity.ok(overrides)
  }

  @PreAuthorize(AUTH_USER)
  @GetMapping("/{overrideId}")
  fun getProductOverride(
    @PathVariable overrideId: String
  ): ResponseEntity<ProductOverrideDto> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      val override = productOverrideService.getProductOverrideById(userId, overrideId)
      ResponseEntity.ok(override)
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @PreAuthorize(AUTH_USER)
  @GetMapping("/product/{productId}")
  fun getProductOverridesByProduct(
    @PathVariable productId: String
  ): ResponseEntity<List<ProductOverrideDto>> {
    val userId = AuthUtils.getCurrentUserId()
    val overrides = productOverrideService.getProductOverridesByProductId(userId, productId)
    return ResponseEntity.ok(overrides)
  }

  @PreAuthorize(AUTH_USER)
  @PutMapping("/{overrideId}")
  fun updateProductOverride(
    @PathVariable overrideId: String,
    @RequestBody request: UpdateProductOverrideRequest
  ): ResponseEntity<ProductOverrideDto> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      val override = productOverrideService.updateProductOverride(userId, overrideId, request)
      ResponseEntity.ok(override)
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @PreAuthorize(AUTH_USER)
  @DeleteMapping("/{overrideId}")
  fun deleteProductOverride(
    @PathVariable overrideId: String
  ): ResponseEntity<Void> {
    val userId = AuthUtils.getCurrentUserId()
    return try {
      productOverrideService.deleteProductOverride(userId, overrideId)
      ResponseEntity.ok().build()
    } catch (e: ServiceException) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }
}
