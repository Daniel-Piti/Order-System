package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.domains.productImage.ProductImageService
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.storage.R2StorageService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class BrandService(
    private val brandRepository: BrandRepository,
    private val productService: ProductService,
    private val r2StorageService: R2StorageService,
    private val productImageService: ProductImageService
) {

    companion object {
        private const val MAX_BRANDS_PER_USER = 1000
    }

    fun getBrandById(userId: String, brandId: Long): BrandDto {
        val brand = brandRepository.findByUserIdAndId(userId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        val imageUrl = r2StorageService.getPublicUrl(brand.s3Key)

        return brand.toDto(imageUrl)
    }

    fun getUserBrands(userId: String): List<BrandDto> {
        val brands = brandRepository.findByUserId(userId)
        
        return brands.map { brand ->
            val imageUrl = r2StorageService.getPublicUrl(brand.s3Key)
            brand.toDto(imageUrl)
        }
    }

    @Transactional
    fun createBrand(userId: String, request: CreateBrandRequest, image: MultipartFile?): Long {
        with(request) {
            FieldValidators.validateNonEmpty(name, "'name'")
        }

        // Validate image if provided
        var s3Key: String? = null
        var fileName: String? = null
        var fileSizeBytes: Long? = null
        var mimeType: String? = null

        if (image != null && !image.isEmpty) {
            productImageService.validateImage(image)
            
            // Generate S3 key with base path
            val originalFileName = image.originalFilename ?: "brand-image"
            val basePath = "$userId/brands"
            s3Key = r2StorageService.generateKey(basePath, originalFileName)
            
            // Upload to R2
            r2StorageService.uploadFile(image, s3Key)
            
            fileName = originalFileName
            fileSizeBytes = image.size
            mimeType = image.contentType ?: "application/octet-stream"
        }

        // Check if user has reached the maximum number of brands
        val existingBrandsCount = brandRepository.findByUserId(userId).size
        if (existingBrandsCount >= MAX_BRANDS_PER_USER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = BrandFailureReason.BRAND_LIMIT_EXCEEDED.userMessage,
                technicalMessage = BrandFailureReason.BRAND_LIMIT_EXCEEDED.technical + "userId=$userId, limit=$MAX_BRANDS_PER_USER",
                severity = SeverityLevel.WARN
            )
        }

        // Check if brand already exists for this user
        if (brandRepository.existsByUserIdAndName(userId, request.name.trim())) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = BrandFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = BrandFailureReason.ALREADY_EXISTS.technical + "userId=$userId, name=${request.name}",
                severity = SeverityLevel.INFO
            )
        }

        val now = LocalDateTime.now()
        val brand = BrandDbEntity(
            userId = userId,
            name = request.name.trim(),
            s3Key = s3Key,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            mimeType = mimeType,
            createdAt = now,
            updatedAt = now
        )

        return brandRepository.save(brand).id
    }

    @Transactional
    fun updateBrand(userId: String, brandId: Long, request: UpdateBrandRequest, image: MultipartFile?): Long {
        val brand = brandRepository.findByUserIdAndId(userId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        with(request) {
            FieldValidators.validateNonEmpty(this.name, "'name'")
        }

        // Check if new brand name already exists for this user (excluding current brand)
        val existingBrand = brandRepository.findByUserIdAndName(userId, request.name.trim())
        if (existingBrand != null && existingBrand.id != brandId) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = BrandFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = BrandFailureReason.ALREADY_EXISTS.technical + "userId=$userId, name=${request.name}",
                severity = SeverityLevel.INFO
            )
        }

        // Handle image upload if provided
        var s3Key = brand.s3Key
        var fileName = brand.fileName
        var fileSizeBytes = brand.fileSizeBytes
        var mimeType = brand.mimeType

        if (image != null && !image.isEmpty) {
            productImageService.validateImage(image)
            
            // Delete old image if exists
            brand.s3Key?.let { oldS3Key ->
                try {
                    r2StorageService.deleteFile(oldS3Key)
                } catch (e: Exception) {
                    // Log but don't fail if deletion fails
                    println("Warning: Failed to delete old brand image: ${e.message}")
                }
            }
            
            // Upload new image
            val originalFileName = image.originalFilename ?: "brand-image"
            val basePath = "$userId/brands"
            s3Key = r2StorageService.generateKey(basePath, originalFileName)
            r2StorageService.uploadFile(image, s3Key)
            
            fileName = originalFileName
            fileSizeBytes = image.size
            mimeType = image.contentType ?: "application/octet-stream"
        }

        val updatedBrand = brand.copy(
            name = request.name.trim(),
            s3Key = s3Key,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            mimeType = mimeType,
            updatedAt = LocalDateTime.now()
        )

        return brandRepository.save(updatedBrand).id
    }

    @Transactional
    fun deleteBrand(userId: String, brandId: Long) {
        val brand = brandRepository.findByUserIdAndId(userId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        // Delete image from R2 if exists
        brand.s3Key?.let { s3Key ->
            try {
                r2StorageService.deleteFile(s3Key)
            } catch (e: Exception) {
                // Log but don't fail if deletion fails
                println("Warning: Failed to delete brand image: ${e.message}")
            }
        }

        // Remove brand from all products that use this brand
        productService.removeBrandFromProducts(userId, brandId)

        brandRepository.deleteById(brandId)
    }
}
