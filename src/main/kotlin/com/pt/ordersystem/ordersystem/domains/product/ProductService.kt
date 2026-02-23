package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.brand.BrandRepository
import com.pt.ordersystem.ordersystem.domains.category.CategoryRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageRepository
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDbEntity
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import com.pt.ordersystem.ordersystem.domains.productImage.models.toDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val productImageRepository: ProductImageRepository,
  private val productOverrideService: ProductOverrideService,
  private val orderService: OrderService,
  private val productOverrideRepository: ProductOverrideRepository,
  private val s3StorageService: S3StorageService,
  private val brandRepository: BrandRepository,
  private val categoryRepository: CategoryRepository,
  private val customerRepository: CustomerRepository,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)
    const val MAXIMUM_PRODUCTS_FOR_CUSTOMER = 1000
    const val MAX_PAGE_SIZE = 100
    private const val MAX_IMAGES_PER_PRODUCT = 5
  }

  private fun validateMaxImagesForProduct(imagesCount: Int) {
    if (imagesCount > MAX_IMAGES_PER_PRODUCT) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Maximum $MAX_IMAGES_PER_PRODUCT images allowed per product",
        technicalMessage = "Attempting to add=$imagesCount, max allowed=$MAX_IMAGES_PER_PRODUCT",
        severity = SeverityLevel.WARN
      )
    }
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
    val brands = brandRepository.findByManagerId(managerId)
    val brandMap = brands.associate { it.id to it.name }
    
    val categories = categoryRepository.findByManagerId(managerId)
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
    val brands = brandRepository.findByManagerId(order.managerId)
    val brandMap = brands.associate { it.id to it.name }
    
    val categories = categoryRepository.findByManagerId(order.managerId)
    val categoryMap = categories.associate { it.id to it.category }

    // If no customer assigned, return products with default prices
    if (order.customerId == null) {
      return products.map { product ->
        val brandName = product.brandId?.let { brandMap[it] }
        val categoryName = product.categoryId?.let { categoryMap[it] }
        product.toDto(brandName = brandName, categoryName = categoryName)
      }
    }

    // Customer exists - get customer and overrides
    val customer = customerRepository.findByManagerIdAndId(order.managerId, order.customerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Customer not found",
        technicalMessage = "Customer with id ${order.customerId} not found for manager ${order.managerId}",
        severity = SeverityLevel.WARN
      )

    val overrides = productOverrideRepository.findByManagerIdAndCustomerId(
      managerId = order.managerId,
      customerId = order.customerId
    )

    // Create a map of productId -> override price for quick lookup
    val overrideMap = overrides.associate { it.productId to it.overridePrice }

    // Map products to DTOs, applying override, discount, and minimum price
    return products.map { product ->
      // Step 1: Start with override price if exists, otherwise default price
      var priceAfterOverride = overrideMap[product.id] ?: product.price
      
      // Step 2: Apply discount percentage if customer has one
      if (customer.discountPercentage > 0) {
        val discountMultiplier = BigDecimal(100 - customer.discountPercentage).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        priceAfterOverride = priceAfterOverride.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP)
      }
      
      // Step 3: Ensure price is not below minimum price
      val finalPrice = priceAfterOverride.max(product.minimumPrice)
      
      val brandName = product.brandId?.let { brandMap[it] }
      val categoryName = product.categoryId?.let { categoryMap[it] }
      product.toDto(brandName = brandName, categoryName = categoryName, price = finalPrice)
    }
  }

  fun getProductById(managerId: String, productId: String): ProductDto {
    val product = getProduct(managerId, productId)

    val brandName = product.brandId?.let {
      try {
        brandRepository.findByManagerIdAndId(managerId, it).name
      } catch (e: Exception) {
        logger.warn("Brand with ID=${product.brandId} not found | $e")
        null
      }
    }

    val categoryName = product.categoryId?.let {
      try {
        categoryRepository.findByManagerIdAndId(managerId, it).category
      } catch (e: Exception) {
        logger.warn("category with ID=${product.categoryId} not found | $e")
        null
      }
    }

    return product.toDto(brandName = brandName, categoryName = categoryName)
  }

  private fun validateProductInfo(managerId: String, productInfo: ProductInfo) {
    with(productInfo) {
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

      // Validate brandId belongs to manager (if provided)
      brandId?.let { brandRepository.findByManagerIdAndId(managerId, it) }

      // Validate categoryId belongs to manager (if provided)
      categoryId?.let { categoryRepository.findByManagerIdAndId(managerId, it) }
    }
  }

  fun getImagesForProduct(productId: String): List<ProductImageDto> {
    val images = productImageRepository.findByProductId(productId)

    return images.map { image ->
      val publicUrl = s3StorageService.getPublicUrl(image.s3Key)
        ?: throw ServiceException(
          status = HttpStatus.INTERNAL_SERVER_ERROR,
          userMessage = "Failed to get image URL",
          technicalMessage = "s3Key is null for image id=${image.id}",
          severity = SeverityLevel.ERROR
        )
      image.toDto(publicUrl)
    }
  }

  @Transactional
  fun createProduct(
    managerId: String,
    productInfo: ProductInfo,
    imagesMetadata: List<ImageMetadata>,
  ): CreateProductResponse {
    // Validate product info first (fail fast)
    validateProductInfo(managerId, productInfo)

    // Check product limit
    val numberOfProducts = productRepository.countByManagerId(managerId)
    if (numberOfProducts >= MAXIMUM_PRODUCTS_FOR_CUSTOMER) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = "You have reached the product limit ($MAXIMUM_PRODUCTS_FOR_CUSTOMER)",
        technicalMessage = "Manager ($managerId) has reached $MAXIMUM_PRODUCTS_FOR_CUSTOMER products",
        severity = SeverityLevel.WARN
      )
    }

    // Image validations
    validateMaxImagesForProduct(imagesMetadata.size)
    imagesMetadata.forEach { s3StorageService.validateImageMetadata(it) }

    val now = LocalDateTime.now()
    val product = ProductDbEntity(
      id = GeneralUtils.genId(),
      managerId = managerId,
      name = productInfo.name,
      brandId = productInfo.brandId,
      categoryId = productInfo.categoryId,
      minimumPrice = productInfo.minimumPrice,
      price = productInfo.price,
      description = productInfo.description,
      createdAt = now,
      updatedAt = now,
    )

    val productId = productRepository.save(product).id

    val imagesPreSignedUrls = generatePreSignedUrlsAndSaveProductImage(managerId, productId, imagesMetadata)

    return CreateProductResponse(
      productId = productId,
      imagesPreSignedUrls = imagesPreSignedUrls
    )
  }

  @Transactional
  fun updateProductInfo(
    managerId: String,
    productId: String,
    productInfo: ProductInfo
  ): String {
    validateProductInfo(managerId, productInfo)

    val product = getProduct(managerId, productId)

    // If minimum price is being increased, update any invalid overrides
    if (productInfo.minimumPrice > product.minimumPrice) {
      productOverrideService.updateInvalidOverridesForProduct(product.managerId, productId, productInfo.minimumPrice)
    }

    val updated = product.copy(
      name = productInfo.name,
      brandId = productInfo.brandId,
      categoryId = productInfo.categoryId,
      minimumPrice = productInfo.minimumPrice,
      price = productInfo.price,
      description = productInfo.description,
      updatedAt = LocalDateTime.now(),
    )

    return productRepository.save(updated).id
  }

  @Transactional
  fun deleteProduct(managerId: String, productId: String) {
    val product = getProduct(managerId, productId)
    
    // Delete all product overrides
    productOverrideService.deleteAllOverridesForProduct(product.managerId, productId)
    // Delete product's images from s3
    productImageRepository.findByProductId(productId).forEach { image -> s3StorageService.deleteFile(image.s3Key) }
    // Delete product's images
    productImageRepository.deleteByProductId(productId)
    // Delete product
    productRepository.delete(product)
  }

  @Transactional
  fun addProductImages(
    managerId: String,
    productId: String,
    imagesMetadataList: List<ImageMetadata>
  ): List<String> {
    // Verify product exists and belongs to manager
    getProduct(managerId, productId)

    // Validate images upfront (fail fast if any invalid)
    val existingImages = productImageRepository.findByProductId(productId)
    val totalImagesAfterUpload = existingImages.size + imagesMetadataList.size

    validateMaxImagesForProduct(totalImagesAfterUpload)
    imagesMetadataList.forEach { metadata -> s3StorageService.validateImageMetadata(metadata) }

    return generatePreSignedUrlsAndSaveProductImage(managerId, productId, imagesMetadataList)
  }

  @Transactional
  fun deleteImages(managerId: String, imageIds: List<Long>) {
    if (imageIds.isEmpty()) return

    // Fetch all images and validate they belong to the manager
    val images = productImageRepository.findAllById(imageIds)
      .filter { it.managerId == managerId }

    if (images.size != imageIds.size) {
      val foundIds = images.map { it.id }.toSet()
      val missingIds = imageIds.filter { it !in foundIds }
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Some images were not found",
        technicalMessage = "Images not found or don't belong to manager: $missingIds",
        severity = SeverityLevel.WARN
      )
    }

    // Delete all images from S3
    images.forEach { image -> s3StorageService.deleteFile(image.s3Key) }
    
    // Delete all images from database
    productImageRepository.deleteAll(images)
  }

  private fun generatePreSignedUrlsAndSaveProductImage(
    managerId: String,
    productId: String,
    imagesMetadataList: List<ImageMetadata>
  ): List<String> {
    val basePath = "managers/$managerId/products/$productId"
    return imagesMetadataList.map { metadata ->
      // Generate preSigned URL (includes validation again for safety)
      val preSignedUrlResult = s3StorageService.generatePreSignedUploadUrl(basePath, metadata)

      // Save image record to database
      val now = LocalDateTime.now()
      val productImage = ProductImageDbEntity(
        productId = productId,
        managerId = managerId,
        s3Key = preSignedUrlResult.s3Key,
        fileName = metadata.fileName,
        fileSizeBytes = metadata.fileSizeBytes,
        mimeType = metadata.contentType,
        createdAt = now,
        updatedAt = now,
      )
      productImageRepository.save(productImage)

      preSignedUrlResult.preSignedUrl
    }
  }

  private fun getProduct(managerId: String, productId: String): ProductDbEntity {
    return productRepository.findByManagerIdAndId(managerId = managerId, id = productId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )
  }

}