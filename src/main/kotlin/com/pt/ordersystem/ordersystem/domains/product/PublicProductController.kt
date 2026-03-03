package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDto
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageRepository
import com.pt.ordersystem.ordersystem.domains.productImage.models.toDto
import com.pt.ordersystem.ordersystem.utils.PageRequestBaseExternal
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Products", description = "Public product API for customers")
@RestController
@RequestMapping("/api/public/products")
class PublicProductController(
  private val productService: ProductService,
  private val managerService: ManagerService,
  private val productImageRepository: ProductImageRepository,
) {

  @GetMapping("/manager/{managerId}/product/{productId}")
  fun getProduct(
    @PathVariable managerId: String,
    @PathVariable productId: String
  ): ResponseEntity<ProductDto> {
    // Validate user exists
    managerService.validateManagerExists(managerId)
    
    val product = productService.getProductById(managerId = managerId, productId = productId)
    return ResponseEntity.ok(product)
  }

  @GetMapping("/manager/{managerId}")
  fun getAllManagerProducts(
    @PathVariable managerId: String,
    pageParams: PageRequestBaseExternal,
    @RequestParam(required = false) categoryId: Long?,
    @RequestParam(required = false) brandId: Long?
  ): ResponseEntity<Page<ProductDto>> {
    managerService.validateManagerExists(managerId)
    val products = productService.getAllProductsForManager(
      managerId = managerId,
      pageRequestBase = pageParams.toPageRequestBase(),
      categoryId = categoryId,
      brandId = brandId
    )
    return ResponseEntity.ok(products)
  }

  @GetMapping("/order/{orderId}")
  fun getAllProductsForOrder(
    @PathVariable orderId: String
  ): ResponseEntity<List<ProductDto>> {
    val products = productService.getAllProductsForOrder(orderId)
    return ResponseEntity.ok(products)
  }

  @GetMapping("/manager/{managerId}/product/{productId}/images")
  fun getProductImages(
    @PathVariable managerId: String,
    @PathVariable productId: String
  ): ResponseEntity<List<ProductImageDto>> {
    val product = productService.getProductById(managerId, productId)
    val images = productImageRepository.findByProductId(product.id).map { it.toDto() }
    return ResponseEntity.ok(images)
  }

}

