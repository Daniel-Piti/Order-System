package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.product.models.CreateProductRequest
import com.pt.ordersystem.ordersystem.domains.product.models.UpdateProductRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Products", description = "Product management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products")
@PreAuthorize(AUTH_MANAGER)
class ProductManagerController(
  private val productService: ProductService
) {

  @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @Operation(
    summary = "Create a new product",
    description = "Create a new product with optional images (up to 5). Provide product data as individual fields."
  )
  fun createProduct(
    @RequestParam("name") name: String,
    @RequestParam(value = "brandId", required = false) brandId: Long?,
    @RequestParam(value = "categoryId", required = false) categoryId: Long?,
    @RequestParam("minimumPrice") minimumPrice: java.math.BigDecimal,
    @RequestParam("price") price: java.math.BigDecimal,
    @RequestParam(value = "description", defaultValue = "") description: String,
    @RequestPart(value = "images", required = false) images: List<MultipartFile>?,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val request = CreateProductRequest(
      name = name,
      brandId = brandId,
      categoryId = categoryId,
      minimumPrice = minimumPrice,
      price = price,
      description = description
    )

    val newProductId = productService.createProductWithImages(user.id, request, images)
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

