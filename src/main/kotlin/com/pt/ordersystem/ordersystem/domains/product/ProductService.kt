package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val productOverrideService: ProductOverrideService,
  private val orderService: OrderService,
  private val productOverrideRepository: ProductOverrideRepository,
  private val productImageService: ProductImageService,
) {

  companion object {
    const val MAXIMUM_PRODUCTS_FOR_CUSTOMER = 1000
    const val MAX_PAGE_SIZE = 100
  }

  fun removeCategoryFromProducts(userId: String, categoryId: Long) {
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
    categoryId: Long?
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
    return if (categoryId == null) {
      // No filter - return all products
      productRepository.findAllByUserId(userId, pageable).map { it.toDto() }
    } else {
      // Filter by specific category
      productRepository.findByUserIdAndCategoryId(userId, categoryId, pageable).map { it.toDto() }
    }
  }

  fun getAllProductsForOrder(orderId: String): List<ProductDto> {
    val order = orderService.getOrderByIdInternal(orderId)

    // Fetch all products for the user
    val products = productRepository.findAllByUserId(order.userId)

    // If no customer assigned, return products with default prices
    if (order.customerId == null) {
      return products.map { it.toDto() }
    }

    // Customer exists - get all overrides for this customer
    val overrides = productOverrideRepository.findByUserIdAndCustomerId(
      userId = order.userId,
      customerId = order.customerId
    )

    // Create a map of productId -> override price for quick lookup
    val overrideMap = overrides.associate { it.productId to it.overridePrice }

    // Map products to DTOs, using override price if available, otherwise special price
    return products.map { product ->
      val effectivePrice = overrideMap[product.id] ?: product.specialPrice
      product.toDto().copy(specialPrice = effectivePrice)
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

  fun createProductWithImages(
    userId: String,
    request: CreateProductRequest,
    images: List<MultipartFile>?
  ): String {
    // Prepare for validation
    val imageFiles = images?.filter { !it.isEmpty } ?: emptyList()

    // Validations
    if (imageFiles.size > 5) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Maximum 5 images allowed per product",
        technicalMessage = "Attempted to upload ${imageFiles.size} images during product creation",
        severity = SeverityLevel.WARN
      )
    }

    imageFiles.forEach { productImageService.validateImage(it) }

    // Create and upload
    val productId = createProduct(userId, request)
    
    if (imageFiles.isNotEmpty()) {
      imageFiles.forEach { image ->
        try {
          productImageService.uploadImageForProduct(userId, productId, image)
        } catch (e: Exception) {
          // Log error but continue with other images
          // If all images fail, product is still created (design decision)
          println("Warning: Failed to upload image for product $productId: ${e.message}")
        }
      }
    }
    
    return productId
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
    
    productImageService.deleteAllImagesForProduct(productId)

    productRepository.delete(product)
  }

}