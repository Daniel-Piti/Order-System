package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.domains.brand.BrandService
import com.pt.ordersystem.ordersystem.domains.category.CategoryService
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
  @Lazy private val categoryService: CategoryService,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)
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

    // Early return if no products
    if (productPage.content.isEmpty()) {
      return PageImpl(emptyList(), pageable, productPage.totalElements)
    }

    // Enrich with brand names and category names
    val brands = brandService.getManagerBrands(managerId)
    val brandMap = brands.associate { it.id to it.name }
    
    val categories = categoryService.getManagerCategories(managerId)
    val categoryMap = categories.associate { it.id to it.category }
    
    val enrichedContent = productPage.content.map { product ->
      val brandName = product.brandId?.let { brandMap[it] }
      if (product.brandId != null && brandName == null) {
        logger.warn("Brand with ID=${product.brandId} not found for manager $managerId")
      }
      
      val categoryName = product.categoryId?.let { categoryMap[it] }
      if (product.categoryId != null && categoryName == null) {
        logger.warn("Category with ID=${product.categoryId} not found for manager $managerId")
      }
      
      product.toDto(brandName = brandName, categoryName = categoryName)
    }

    return PageImpl(enrichedContent, pageable, productPage.totalElements)
  }

  fun getAllProductsForOrder(orderId: String): List<ProductDto> {
    val order = orderService.getOrderByIdInternal(orderId)

    // Fetch all products for the manager
    val products = productRepository.findAllByManagerId(order.managerId)

    // Fetch brands and categories for enrichment
    val brands = brandService.getManagerBrands(order.managerId)
    val brandMap = brands.associate { it.id to it.name }
    
    val categories = categoryService.getManagerCategories(order.managerId)
    val categoryMap = categories.associate { it.id to it.category }

    // If no customer assigned, return products with default prices
    if (order.customerId == null) {
      return products.map { product ->
        val brandName = product.brandId?.let { brandMap[it] }
        val categoryName = product.categoryId?.let { categoryMap[it] }
        product.toDto(brandName = brandName, categoryName = categoryName)
      }
    }

    // Customer exists - get all overrides for this customer
    val overrides = productOverrideRepository.findByManagerIdAndCustomerId(
      managerId = order.managerId,
      customerId = order.customerId
    )

    // Create a map of productId -> override price for quick lookup
    val overrideMap = overrides.associate { it.productId to it.overridePrice }

    // Map products to DTOs, using override price if available, otherwise default price
    return products.map { product ->
      val effectivePrice = overrideMap[product.id] ?: product.price
      val brandName = product.brandId?.let { brandMap[it] }
      val categoryName = product.categoryId?.let { categoryMap[it] }
      product.toDto(brandName = brandName, categoryName = categoryName).copy(price = effectivePrice)
    }
  }

  fun getProductById(managerId: String, productId: String): ProductDto {
    val product = productRepository.findByManagerIdAndId(managerId = managerId, id = productId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )

    val brandName = product.brandId?.let {
      try {
        brandService.getBrandById(managerId, it).name
      } catch (e: Exception) {
        logger.warn("Brand with ID=${product.brandId} not found")
        null
      }
    }

    val categoryName = product.categoryId?.let {
      try {
        categoryService.getCategoryById(managerId, it).category
      } catch (e: Exception) {
        logger.warn("category with ID=${product.categoryId} not found")
        null
      }
    }

    return product.toDto(brandName = brandName, categoryName = categoryName)
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
  fun updateProduct(managerId: String, productId: String, request: UpdateProductRequest): String {

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

    val product = productRepository.findByManagerIdAndId(managerId = managerId, id = productId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )

    // If minimum price is being increased, update any invalid overrides
    if (request.minimumPrice > product.minimumPrice) {
      productOverrideService.updateInvalidOverridesForProduct(product.managerId, productId, request.minimumPrice)
    }

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
  fun deleteProduct(managerId: String, productId: String) {
    val product = productRepository.findByManagerIdAndId(managerId = managerId, id = productId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )

    productOverrideService.deleteAllOverridesForProduct(product.managerId, productId)
    
    productImageService.deleteAllImagesForProduct(productId)

    productRepository.delete(product)
  }

}