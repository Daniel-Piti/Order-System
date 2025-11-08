package com.pt.ordersystem.ordersystem.domains.productImage

import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.config.ConfigProvider
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDbEntity
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import com.pt.ordersystem.ordersystem.domains.productImage.models.toDto
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.storage.R2StorageService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ProductImageService(
  private val productImageRepository: ProductImageRepository,
  private val productRepository: ProductRepository,
  private val r2StorageService: R2StorageService,
  private val configProvider: ConfigProvider
) {
  companion object {
    private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    private const val MAX_IMAGES_PER_PRODUCT = 5
  }

  fun validateAndUploadImageForProduct(
    userId: String,
    productId: String,
    file: MultipartFile
  ): ProductImageDto {
    // Verify product exists and belongs to user
    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Product not found",
        technicalMessage = "Product with id $productId not found",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.userId)

    // Check existing images count
    val existingImages = productImageRepository.findByProductId(productId)

    if (existingImages.size >= MAX_IMAGES_PER_PRODUCT) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Maximum $MAX_IMAGES_PER_PRODUCT images allowed per product",
        technicalMessage = "Product $productId already has ${existingImages.size} images",
        severity = SeverityLevel.WARN
      )
    }

    return uploadImageForProduct(userId, productId, file)
  }

  fun uploadImageForProduct(
    userId: String,
    productId: String,
    file: MultipartFile
  ): ProductImageDto {

    // Generate S3 key with base path
    val fileName = file.originalFilename ?: "image"
    val basePath = "users/$userId/products/$productId"
    val s3Key = r2StorageService.generateKey(basePath, fileName)

    // Upload to R2
    val publicUrl = r2StorageService.uploadFile(file, s3Key)

    // Save to database
    val productImage = ProductImageDbEntity(
      productId = productId,
      userId = userId,
      s3Key = s3Key,
      fileName = file.originalFilename ?: "image",
      fileSizeBytes = file.size,
      mimeType = file.contentType ?: "application/octet-stream",
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    val savedImage = productImageRepository.save(productImage)
    return savedImage.toDto(publicUrl)
  }

  fun getImagesForProduct(productId: String): List<ProductImageDto> {
    val images = productImageRepository.findByProductId(productId)

    return images.map { image ->
      val publicUrl = r2StorageService.getPublicUrl(image.s3Key)
        ?: throw ServiceException(
          status = HttpStatus.INTERNAL_SERVER_ERROR,
          userMessage = "Failed to get image URL",
          technicalMessage = "s3Key is null for image id=${image.id}",
          severity = SeverityLevel.ERROR
        )
      image.toDto(publicUrl)
    }
  }

  fun deleteImage(userId: String, imageId: Long) {
    val image = productImageRepository.findById(imageId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Image not found",
        technicalMessage = "Image with id $imageId not found",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(image.userId)

    // Delete from R2
    r2StorageService.deleteFile(image.s3Key)

    // Delete from database
    productImageRepository.delete(image)
  }

  fun deleteAllImagesForProduct(productId: String) {
    val images = productImageRepository.findByProductId(productId)
    images.forEach { image ->
      r2StorageService.deleteFile(image.s3Key)
    }
    productImageRepository.deleteByProductId(productId)
  }

  fun validateImage(file: MultipartFile) {
    if (file.isEmpty) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "File cannot be empty",
        technicalMessage = "Empty file uploaded",
        severity = SeverityLevel.WARN
      )
    }

    val maxSizeBytes = configProvider.maxUploadFileSizeMb * 1024L * 1024L

    if (file.size > maxSizeBytes) {
      throw ServiceException(
        status = HttpStatus.PAYLOAD_TOO_LARGE,
        userMessage = "File size exceeds the maximum allowed limit of ${configProvider.maxUploadFileSizeMb}MB",
        technicalMessage = "File size: ${file.size} bytes, max: $maxSizeBytes bytes",
        severity = SeverityLevel.WARN
      )
    }

    val contentType = file.contentType ?: ""
    if (!ALLOWED_MIME_TYPES.contains(contentType.lowercase())) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only ${ALLOWED_EXTENSIONS.toList()} images are allowed",
        technicalMessage = "Invalid MIME type: $contentType",
        severity = SeverityLevel.WARN
      )
    }

    // Additional validation: check file extension
    val fileName = file.originalFilename ?: ""
    val extension = fileName.substringAfterLast(".", "").lowercase()
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only ${ALLOWED_EXTENSIONS.toList()} images are allowed",
        technicalMessage = "Invalid file extension: $extension",
        severity = SeverityLevel.WARN
      )
    }
  }
}
