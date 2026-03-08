package com.pt.ordersystem.ordersystem.domains.product.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.domains.product.models.CreateProductRequest
import com.pt.ordersystem.ordersystem.domains.product.models.CreateProductResponse
import com.pt.ordersystem.ordersystem.domains.product.models.ProductInfo
import com.pt.ordersystem.ordersystem.domains.product.models.ProductPrivateDto
import com.pt.ordersystem.ordersystem.domains.product.models.UploadProductImagesResponse
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
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
@PreAuthorize(AUTH_MANAGER)
class ProductManagerController(
  private val productService: ProductService
) {

  @GetMapping
  fun getAllProducts(@AuthenticationPrincipal manager: AuthUser): ResponseEntity<List<ProductPrivateDto>> {
    val products = productService.getAllProductsForManager(manager.id)
    return ResponseEntity.ok(products.map { it.toPrivateDto() })
  }

  @PostMapping
  fun createProduct(
    @RequestBody request: CreateProductRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<CreateProductResponse> {
    val response = productService.createProduct(manager.id, request.productInfo, request.imagesMetadata)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @PutMapping("/{productId}")
  fun updateProductInfo(
    @PathVariable productId: String,
    @RequestBody productInfo: ProductInfo,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<ProductPrivateDto> {
    val product = productService.updateProductInfo(manager.id, productId, productInfo)
    return ResponseEntity.ok(product.toPrivateDto())
  }

  @DeleteMapping("/{productId}")
  fun deleteProduct(
    @PathVariable productId: String,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    productService.deleteProduct(manager.id, productId)
    return ResponseEntity.ok("Product deleted successfully")
  }

  @PostMapping("/{productId}/images")
  fun uploadImages(
    @PathVariable productId: String,
    @RequestBody imagesMetadata: List<ImageMetadata>,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<UploadProductImagesResponse> {
    val imageUploads = productService.addProductImages(manager.id, productId, imagesMetadata)
    return ResponseEntity.status(HttpStatus.CREATED).body(UploadProductImagesResponse(imagesPreSignedUrls = imageUploads))
  }

  @DeleteMapping("/{productId}/images")
  fun deleteImages(
    @PathVariable productId: String,
    @RequestBody imageIds: List<Long>,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    productService.deleteImages(manager.id, productId, imageIds)
    return ResponseEntity.ok("Images deleted successfully")
  }
}

