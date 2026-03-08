package com.pt.ordersystem.ordersystem.domains.product.controllers

import com.pt.ordersystem.ordersystem.domains.product.models.ProductPublicDto
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageRepository
import com.pt.ordersystem.ordersystem.domains.productImage.models.toDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Products", description = "Public product API for customers")
@RestController
@RequestMapping("/api/public/products")
class ProductPublicController(
  private val productService: ProductService,
  private val managerService: ManagerService,
  private val productImageRepository: ProductImageRepository,
) {

  @GetMapping("/manager/{managerId}")
  fun getAllManagerProducts(@PathVariable managerId: String): ResponseEntity<List<ProductPublicDto>> {
    managerService.validateManagerExists(managerId)
    val products = productService.getAllProductsForManager(managerId)
    return ResponseEntity.ok(products.map { it.toPublicDto() })
  }

  @GetMapping("/order/{orderId}")
  fun getAllProductsForOrder(@PathVariable orderId: String): ResponseEntity<List<ProductPublicDto>> {
    val products = productService.getAllProductsForOrder(orderId)
    return ResponseEntity.ok(products.map { it.toPublicDto() })
  }

  @GetMapping("/manager/{managerId}/product/{productId}/images")
  fun getProductImages(
    @PathVariable managerId: String,
    @PathVariable productId: String
  ): ResponseEntity<List<ProductImageDto>> {
    productService.validateProductExistsForManager(managerId, productId)
    val productImages = productImageRepository.findByManagerIdAndProductId(managerId, productId)
    return ResponseEntity.ok(productImages.map { it.toDto() })
  }

}

