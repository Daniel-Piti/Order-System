package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val productOverrideService: ProductOverrideService,
  private val orderService: OrderService,
) {

  companion object {
    const val MAXIMUM_PRODUCTS_FOR_CUSTOMER = 1000
    const val MAX_PAGE_SIZE = 100
  }

  fun removeCategoryFromProducts(userId: String, categoryId: String) {
    val productsWithCategory = productRepository.findByUserIdAndCategoryId(userId, categoryId)
    productsWithCategory.forEach { product ->
      val updatedProduct = product.copy(
        categoryId = null,
        updatedAt = LocalDateTime.now()
      )
      productRepository.save(updatedProduct)
    }
  }

  fun getAllProductsForUser(
    userId: String,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    categoryId: String?
  ): Page<ProductDto> {
    // Enforce max page size
    val validatedSize = size.coerceAtMost(MAX_PAGE_SIZE)

    // Create sort based on direction
    val sort = if (sortDirection.uppercase() == "DESC") {
      Sort.by(sortBy).descending()
    } else {
      Sort.by(sortBy).ascending()
    }

    // Create pageable with sort
    val pageable = PageRequest.of(page, validatedSize, sort)

    // Fetch products based on category filter
    return when {
      categoryId == null || categoryId.isBlank() -> {
        // No filter - return all products
        productRepository.findAllByUserId(userId, pageable).map { it.toDto() }
      }
      categoryId.uppercase() == "NONE" -> {
        // Filter for products with no category (categoryId IS NULL)
        productRepository.findByUserIdAndCategoryIdIsNull(userId, pageable).map { it.toDto() }
      }
      else -> {
        // Filter by specific category
        productRepository.findByUserIdAndCategoryId(userId, categoryId, pageable).map { it.toDto() }
      }
    }
  }

  fun getAllProductsForOrder(orderId: String): List<ProductDto> {
    val order = orderService.getOrderById(orderId)
    val products = productRepository.findAllByUserId(order.userId)

    // If no customer assigned, return products with default prices
    val customerId = order.customerId ?: return products.map { it.toDto() }

    // Apply customer-specific price overrides
    val productOverrides = productOverrideService
      .getProductOverridesByCustomerId(order.userId, customerId)
      .associateBy { it.productId }

    return products.map { product ->
      val override = productOverrides[product.id]
      product.copy(
        specialPrice = override?.overridePrice ?: product.specialPrice,
      ).toDto()
    }
  }

  fun getProductById(productId: String): ProductDto {
    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.userId)

    return product.toDto()
  }

  fun createProduct(userId: String, request: CreateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(originalPrice)
      FieldValidators.validatePrice(specialPrice)

      // Validate that special price is not greater than original price
      if (specialPrice > originalPrice) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Special price cannot be greater than original price",
          technicalMessage = "specialPrice=$specialPrice > originalPrice=$originalPrice",
          severity = SeverityLevel.WARN
        )
      }
    }

    val numberOfProducts = productRepository.findAllByUserId(userId).size

    if(numberOfProducts >= MAXIMUM_PRODUCTS_FOR_CUSTOMER) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = "You have reached the product limit ($MAXIMUM_PRODUCTS_FOR_CUSTOMER)",
        technicalMessage = "User ($userId) has reached $MAXIMUM_PRODUCTS_FOR_CUSTOMER products",
        severity = SeverityLevel.WARN
      )
    }

    val product = ProductDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      name = request.name,
      categoryId = request.categoryId,
      originalPrice = request.originalPrice,
      specialPrice = request.specialPrice,
      description = request.description,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return productRepository.save(product).id
  }

  fun updateProduct(productId: String, request: UpdateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(originalPrice)
      FieldValidators.validatePrice(specialPrice)

      // Validate that special price is not greater than original price
      if (specialPrice > originalPrice) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Special price cannot be greater than original price",
          technicalMessage = "specialPrice=$specialPrice > originalPrice=$originalPrice",
          severity = SeverityLevel.WARN
        )
      }
    }

    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.userId)

    val updated = product.copy(
      name = request.name,
      categoryId = request.categoryId,
      originalPrice = request.originalPrice,
      specialPrice = request.specialPrice,
      description = request.description,
      updatedAt = LocalDateTime.now(),
    )

    return productRepository.save(updated).id
  }

  fun deleteProduct(productId: String) {
    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.userId)

    productOverrideService.deleteAllOverridesForProduct(product.userId, productId)

    productRepository.delete(product)
  }

}