package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.brand.BrandRepository
import com.pt.ordersystem.ordersystem.domains.category.CategoryRepository
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.product.helpers.ProductValidators
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageRepository
import com.pt.ordersystem.ordersystem.domains.productImage.helpers.ProductImageValidators
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDbEntity
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.slf4j.LoggerFactory
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
  }

  fun removeCategoryFromProducts(managerId: String, categoryId: Long) =
    productRepository.removeCategoryFromProducts(managerId, categoryId)

  fun removeBrandFromProducts(managerId: String, brandId: Long) =
    productRepository.removeBrandFromProducts(managerId, brandId)

  fun getAllProductsForManager(managerId: String): List<Product> =
    productRepository.getManagersProducts(managerId)

  fun getAllProductsForOrder(orderId: String): List<Product> {
    val order = orderService.getOrderById(orderId)
    val products = productRepository.getManagersProducts(order.managerId)

    if (order.customerId == null) return products

    val customer = customerRepository.findByManagerIdAndId(order.managerId, order.customerId)
    val overrideMap = productOverrideRepository.getAllForManagerIdAndCustomerId(order.managerId, order.customerId)
      .associate { it.productId to it.overridePrice }

    return products.map { product ->
      var priceAfterOverride = overrideMap[product.id] ?: product.price
      if (customer.discountPercentage > 0) {
        val discountMultiplier = BigDecimal(100 - customer.discountPercentage).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        priceAfterOverride = priceAfterOverride.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP)
      }
      val finalPrice = priceAfterOverride.max(product.minimumPrice)
      product.copy(price = finalPrice)
    }
  }

  fun validateProductExistsForManager(managerId: String, productId: String) {
    productRepository.getProductEntityByManagerIdAndId(managerId, productId)
  }

  private fun validateProductInfo(managerId: String, productInfo: ProductInfo) {
    with(productInfo) {
      ProductValidators.validateProductInfo(productInfo)
      ProductValidators.validatePriceHigherOrEqualToMinPrice(price, minimumPrice)

      // Validate brandId belongs to manager (if provided)
      brandId?.let { brandRepository.findByManagerIdAndId(managerId, it) }

      // Validate categoryId belongs to manager (if provided)
      categoryId?.let { categoryRepository.findByManagerIdAndId(managerId, it) }
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
    val productCount = productRepository.countByManagerId(managerId)
    ProductValidators.validateMaxProductPerCustomer(productCount, managerId)

    // Image validations
    ProductImageValidators.validateMaxImagesForProduct(imagesMetadata.size)
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

    val saved = productRepository.save(product)

    val imagesPreSignedUrls = generatePreSignedUrlsAndSaveProductImage(managerId, product.id, imagesMetadata)

    val productModel = productRepository.getProductByManagerIdAndId(managerId, saved.id)
    return CreateProductResponse(
      product = productModel.toPrivateDto(),
      imagesPreSignedUrls = imagesPreSignedUrls
    )
  }

  @Transactional
  fun updateProductInfo(
    managerId: String,
    productId: String,
    productInfo: ProductInfo
  ): Product {
    validateProductInfo(managerId, productInfo)

    val entity = productRepository.findByManagerIdAndId(managerId, productId)

    // If minimum price is being increased, update any invalid overrides
    if (productInfo.minimumPrice > entity.minimumPrice) {
      productOverrideService.updateInvalidOverridesForProduct(entity.managerId, productId, productInfo.minimumPrice)
    }

    val updated = entity.copy(
      name = productInfo.name,
      brandId = productInfo.brandId,
      categoryId = productInfo.categoryId,
      minimumPrice = productInfo.minimumPrice,
      price = productInfo.price,
      description = productInfo.description,
      updatedAt = LocalDateTime.now(),
    )

    productRepository.save(updated)
    return productRepository.getProductByManagerIdAndId(managerId, productId)
  }

  @Transactional
  fun deleteProduct(managerId: String, productId: String) {
    val product = productRepository.findByManagerIdAndId(managerId, productId)

    // Delete all product overrides
    productOverrideService.deleteAllOverridesForProduct(product.managerId, productId)
    // Delete product's images from s3
    productImageRepository.findByManagerIdAndProductId(managerId, productId).forEach { image -> s3StorageService.deleteFile(image.s3Key) }
    // Delete product's images
    productImageRepository.deleteByManagerIdAndProductId(managerId, productId)
    // Delete product
    productRepository.delete(product)
  }

  @Transactional
  fun addProductImages(
    managerId: String,
    productId: String,
    imagesMetadataList: List<ImageMetadata>
  ): List<String> {
    val product = productRepository.findByManagerIdAndId(managerId, productId)

    // Validate images upfront (fail fast if any invalid)
    val existingImages = productImageRepository.findByManagerIdAndProductId(managerId, product.id)
    val totalImagesAfterUpload = existingImages.size + imagesMetadataList.size

    ProductImageValidators.validateMaxImagesForProduct(totalImagesAfterUpload)
    imagesMetadataList.forEach { metadata -> s3StorageService.validateImageMetadata(metadata) }

    return generatePreSignedUrlsAndSaveProductImage(managerId, product.id, imagesMetadataList)
  }

  @Transactional
  fun deleteImages(managerId: String, productId: String, imageIds: List<Long>) {
    if (imageIds.isEmpty()) return

    val images = productImageRepository.findAllById(imageIds)
      .filter { it.managerId == managerId && it.productId == productId }

    if (images.size != imageIds.size) {
      val foundIds = images.map { it.id }.toSet()
      val skippedIds = imageIds.filter { it !in foundIds }
      logger.warn("Not all requested images belong to this product; deleting only matching ones. skippedIds=$skippedIds, productId=$productId, managerId=$managerId")
    }

    images.forEach { image -> s3StorageService.deleteFile(image.s3Key) }
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

}