package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDto
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Products", description = "Public product API for customers")
@RestController
@RequestMapping("/api/public/products")
class PublicProductController(
  private val productService: ProductService,
  private val managerService: ManagerService
) {

  @GetMapping("/manager/{managerId}/product/{productId}")
  fun getProduct(
    @PathVariable managerId: String,
    @PathVariable productId: String
  ): ResponseEntity<ProductDto> {
    // Validate user exists
    managerService.getManagerById(managerId)
    
    val product = productService.getProductById(managerId = managerId, productId = productId)
    return ResponseEntity.ok(product)
  }

  @GetMapping("/manager/{managerId}")
  fun getAllManagerProducts(
    @PathVariable managerId: String,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "name") sortBy: String,
    @RequestParam(defaultValue = "ASC") sortDirection: String,
    @RequestParam(required = false) categoryId: Long?,
    @RequestParam(required = false) brandId: Long?
  ): ResponseEntity<Page<ProductDto>> {
    // Validate user exists first - will throw 404 if not found
    managerService.getManagerById(managerId)
    
    val products = productService.getAllProductsForManager(
      managerId = managerId,
      page = page,
      size = size,
      sortBy = sortBy,
      sortDirection = sortDirection,
      categoryId = categoryId,
      brandId = brandId
    )
    return ResponseEntity.ok(products)
  }

  @GetMapping("/order/{orderId}")
  fun getAllProductsForOrder(
    @PathVariable orderId: Long
  ): ResponseEntity<List<ProductDto>> {
    val products = productService.getAllProductsForOrder(orderId)
    return ResponseEntity.ok(products)
  }

  @GetMapping("/manager/{managerId}/product/{productId}/images")
  fun getProductImages(
    @PathVariable managerId: String,
    @PathVariable productId: String
  ): ResponseEntity<List<ProductImageDto>> {
    // Validate product belongs to manager (this also implicitly validates manager exists)
    productService.getProductById(managerId, productId)
    
    val images = productService.getImagesForProduct(productId)
    return ResponseEntity.ok(images)
  }

}

