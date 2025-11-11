package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.domains.brand.BrandService
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val productOverrideService: ProductOverrideService,
  private val orderService: OrderService,
  private val productOverrideRepository: ProductOverrideRepository,
  private val productImageService: ProductImageService,
  @Lazy private val brandService: BrandService,
) {

  companion object {
    const val MAXIMUM_PRODUCTS_FOR_CUSTOMER = 1000
    const val MAX_PAGE_SIZE = 100
  }

  fun removeCategoryFromProducts(managerId: String, categoryId: Long) =
    productRepository.removeCategoryFromProducts(managerId, categoryId, LocalDateTime.now())

  fun removeBrandFromProducts(managerId: String, brandId: Long) =
    productRepository.removeBrandFromProducts(managerId, brandId, LocalDateTime.now())

  fun getAllProductsForManager(
    managerId: String,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    categoryId: Long?,
    brandId: Long?
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

    // Fetch products based on filters
    val productPage = when {
      categoryId != null && brandId != null -> {
        // Filter by both category and brand
        productRepository.findByManagerIdAndCategoryIdAndBrandId(managerId, categoryId, brandId, pageable)
      }
      categoryId != null -> {
        // Filter by category only
        productRepository.findByManagerIdAndCategoryId(managerId, categoryId, pageable)
      }
      brandId != null -> {
        // Filter by brand only
        productRepository.findByManagerIdAndBrandId(managerId, brandId, pageable)
      }
      else -> {
        // No filter - return all products
        productRepository.findAllByManagerId(managerId, pageable)
      }
    }

    // Enrich with brand names
    val brands = brandService.getManagerBrands(managerId)
    val brandMap = brands.associateBy { it.id }
    
    val enrichedContent = productPage.content.map { product ->
      val brandName = product.brandId?.let { brandMap[it]?.name }
      product.toDto(brandName = brandName)
    }

    return org.springframework.data.domain.PageImpl(enrichedContent, pageable, productPage.totalElements)
  }

  fun getAllProductsForOrder(orderId: String): List<ProductDto> {
    val order = orderService.getOrderByIdInternal(orderId)

    // Fetch all products for the manager
    val products = productRepository.findAllByManagerId(order.userId)

    // Fetch brands for enrichment
    val brands = brandService.getManagerBrands(order.userId)
    val brandMap = brands.associateBy { it.id }

    // If no customer assigned, return products with default prices
    if (order.customerId == null) {
      return products.map { product ->
        val brandName = product.brandId?.let { brandMap[it]?.name }
        product.toDto(brandName = brandName)
      }
    }

    // Customer exists - get all overrides for this customer
    val overrides = productOverrideRepository.findByManagerIdAndCustomerId(
      managerId = order.userId,
      customerId = order.customerId
    )

    // Create a map of productId -> override price for quick lookup
    val overrideMap = overrides.associate { it.productId to it.overridePrice }

    // Map products to DTOs, using override price if available, otherwise default price
    return products.map { product ->
      val effectivePrice = overrideMap[product.id] ?: product.price
      val brandName = product.brandId?.let { brandMap[it]?.name }
      product.toDto(brandName = brandName).copy(price = effectivePrice)
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

    AuthUtils.checkOwnership(product.managerId)

    // Fetch brand name if brandId exists
    val brandName = product.brandId?.let {
      try {
        brandService.getBrandById(product.managerId, it).name
      } catch (e: Exception) {
        null // Brand might have been deleted
      }
    }

    return product.toDto(brandName = brandName)
  }

  @Transactional
  fun createProduct(managerId: String, request: CreateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(minimumPrice)
      FieldValidators.validatePrice(price)

      // Validate that price is not lower than minimum price
      if (price < minimumPrice) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Price cannot be lower than minimum price",
          technicalMessage = "price=$price < minimumPrice=$minimumPrice",
          severity = SeverityLevel.WARN
        )
      }
    }

    val numberOfProducts = productRepository.findAllByManagerId(managerId).size

    if(numberOfProducts >= MAXIMUM_PRODUCTS_FOR_CUSTOMER) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = "You have reached the product limit ($MAXIMUM_PRODUCTS_FOR_CUSTOMER)",
        technicalMessage = "Manager ($managerId) has reached $MAXIMUM_PRODUCTS_FOR_CUSTOMER products",
        severity = SeverityLevel.WARN
      )
    }

    val product = ProductDbEntity(
      id = GeneralUtils.genId(),
      managerId = managerId,
      name = request.name,
      brandId = request.brandId,
      categoryId = request.categoryId,
      minimumPrice = request.minimumPrice,
      price = request.price,
      description = request.description,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return productRepository.save(product).id
  }

  @Transactional
  fun createProductWithImages(
    managerId: String,
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
    val productId = createProduct(managerId, request)
    
    if (imageFiles.isNotEmpty()) {
      imageFiles.forEach { image ->
        try {
          productImageService.uploadImageForProduct(managerId, productId, image)
        } catch (e: Exception) {
          // Log error but continue with other images
          // If all images fail, product is still created (design decision)
          println("Warning: Failed to upload image for product $productId: ${e.message}")
        }
      }
    }
    
    return productId
  }

  @Transactional
  fun updateProduct(productId: String, request: UpdateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(minimumPrice)
      FieldValidators.validatePrice(price)

      // Validate that price is not lower than minimum price
      if (price < minimumPrice) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Price cannot be lower than minimum price",
          technicalMessage = "price=$price < minimumPrice=$minimumPrice",
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

    AuthUtils.checkOwnership(product.managerId)

    val updated = product.copy(
      name = request.name,
      brandId = request.brandId,
      categoryId = request.categoryId,
      minimumPrice = request.minimumPrice,
      price = request.price,
      description = request.description,
      updatedAt = LocalDateTime.now(),
    )

    return productRepository.save(updated).id
  }

  @Transactional
  fun deleteProduct(productId: String) {
    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.managerId)

    productOverrideService.deleteAllOverridesForProduct(product.managerId, productId)
    
    productImageService.deleteAllImagesForProduct(productId)

    productRepository.delete(product)
  }

}