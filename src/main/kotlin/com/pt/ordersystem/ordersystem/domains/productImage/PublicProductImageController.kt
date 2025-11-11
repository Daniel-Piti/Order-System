package com.pt.ordersystem.ordersystem.domains.productImage

import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Product Images", description = "Public product image API for customers")
@RestController
@RequestMapping("/api/public/products")
class PublicProductImageController(
  private val productImageService: ProductImageService,
  private val managerService: ManagerService
) {

  @GetMapping("/manager/{managerId}/product/{productId}/images")
  fun getProductImages(
    @PathVariable managerId: String,
    @PathVariable productId: String
  ): ResponseEntity<List<ProductImageDto>> {
    // Validate user exists
    managerService.getManagerById(managerId)
    
    val images = productImageService.getImagesForProduct(productId)
    return ResponseEntity.ok(images)
  }
}

