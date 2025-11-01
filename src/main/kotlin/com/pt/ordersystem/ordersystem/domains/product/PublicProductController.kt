package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDto
import com.pt.ordersystem.ordersystem.domains.user.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Products", description = "Public product API for customers")
@RestController
@RequestMapping("/api/public/products")
class PublicProductController(
  private val productService: ProductService,
  private val userService: UserService
) {

  @GetMapping("/user/{userId}/product/{productId}")
  fun getProduct(
    @PathVariable userId: String,
    @PathVariable productId: String
  ): ResponseEntity<ProductDto> {
    // Validate user exists
    userService.getUserById(userId)
    
    val product = productService.getProductById(productId)
    return ResponseEntity.ok(product)
  }

  @GetMapping("/user/{userId}")
  fun getAllUserProducts(
    @PathVariable userId: String,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "name") sortBy: String,
    @RequestParam(defaultValue = "ASC") sortDirection: String,
    @RequestParam(required = false) categoryId: Long?
  ): ResponseEntity<Page<ProductDto>> {
    // Validate user exists first - will throw 404 if not found
    userService.getUserById(userId)
    
    val products = productService.getAllProductsForUser(
      userId = userId,
      page = page,
      size = size,
      sortBy = sortBy,
      sortDirection = sortDirection,
      categoryId = categoryId
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

}

