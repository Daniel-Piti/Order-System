package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BrandService(
    private val brandRepository: BrandRepository,
    private val productService: ProductService,
    private val s3StorageService: S3StorageService
) {

    companion object {
        private const val MAX_BRANDS_PER_MANAGER = 1000
    }

    fun getBrandById(managerId: String, brandId: Long): BrandDto {
        val brand = brandRepository.findByManagerIdAndId(managerId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        val imageUrl = s3StorageService.getPublicUrl(brand.s3Key)

        return brand.toDto(imageUrl)
    }

    fun getManagerBrands(managerId: String): List<BrandDto> {
        val brands = brandRepository.findByManagerId(managerId)
        
        return brands.map { brand ->
            val imageUrl = s3StorageService.getPublicUrl(brand.s3Key)
            brand.toDto(imageUrl)
        }
    }

    @Transactional
    fun createBrand(managerId: String, request: CreateBrandRequest): BrandCreateResponse {
        with(request) {
            FieldValidators.validateNonEmpty(name, "'name'")
        }

        // Check if manager has reached the maximum number of brands
        val existingBrandsCount = brandRepository.findByManagerId(managerId).size
        if (existingBrandsCount >= MAX_BRANDS_PER_MANAGER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = BrandFailureReason.BRAND_LIMIT_EXCEEDED.userMessage,
                technicalMessage = BrandFailureReason.BRAND_LIMIT_EXCEEDED.technical + "managerId=$managerId, limit=$MAX_BRANDS_PER_MANAGER",
                severity = SeverityLevel.WARN
            )
        }

        // Check if brand already exists for this manager
        if (brandRepository.existsByManagerIdAndName(managerId, request.name.trim())) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = BrandFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = BrandFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, name=${request.name}",
                severity = SeverityLevel.INFO
            )
        }

        // Handle image: generate presigned URL if image metadata provided
        val preSignedUrlResult = request.imageMetadata?.let { imageMetadata ->
            s3StorageService.generatePreSignedUploadUrl(
                basePath = "managers/$managerId/brands",
                imageMetadata = imageMetadata
            )
        }

        // Create brand
        val now = LocalDateTime.now()
        val brand = BrandDbEntity(
            managerId = managerId,
            name = request.name.trim(),
            s3Key = preSignedUrlResult?.s3Key,
            fileName = request.imageMetadata?.fileName,
            fileSizeBytes = request.imageMetadata?.fileSizeBytes,
            mimeType = request.imageMetadata?.contentType,
            createdAt = now,
            updatedAt = now
        )

        val brandId = brandRepository.save(brand).id

        return BrandCreateResponse(
            brandId = brandId,
            preSignedUrl = preSignedUrlResult?.preSignedUrl
        )
    }

    @Transactional
    fun updateBrand(managerId: String, brandId: Long, request: UpdateBrandRequest): BrandUpdateResponse {
        val brand = brandRepository.findByManagerIdAndId(managerId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        with(request) {
            FieldValidators.validateNonEmpty(this.name, "'name'")
        }

        // Check if new brand name already exists for this manager (excluding current brand)
        val existingBrand = brandRepository.findByManagerIdAndName(managerId, request.name.trim())
        if (existingBrand != null && existingBrand.id != brandId) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = BrandFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = BrandFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, name=${request.name}",
                severity = SeverityLevel.INFO
            )
        }

        // Handle image: generate preSigned URL if image metadata provided
        val preSignedUrlResult = request.imageMetadata?.let { imageMetadata ->
            // Delete old image if exists
            brand.s3Key?.let { oldS3Key ->
                try {
                    s3StorageService.deleteFile(oldS3Key)
                } catch (e: Exception) {
                    println("Warning: Failed to delete old brand image: ${e.message}")
                }
            }

            // Generate preSigned URL (includes all validation, S3 key generation, and URL creation)
            s3StorageService.generatePreSignedUploadUrl(
                basePath = "managers/$managerId/brands",
                imageMetadata = imageMetadata
            )
        }

        // Update brand
        val updatedBrand = brand.copy(
            name = request.name.trim(),
            s3Key = preSignedUrlResult?.s3Key ?: brand.s3Key,
            fileName = request.imageMetadata?.fileName ?: brand.fileName,
            fileSizeBytes = request.imageMetadata?.fileSizeBytes ?: brand.fileSizeBytes,
            mimeType = request.imageMetadata?.contentType ?: brand.mimeType,
            updatedAt = LocalDateTime.now()
        )

        val updatedBrandId = brandRepository.save(updatedBrand).id

        return BrandUpdateResponse(
            brandId = updatedBrandId,
            preSignedUrl = preSignedUrlResult?.preSignedUrl
        )
    }

    @Transactional
    fun deleteBrand(managerId: String, brandId: Long) {
        val brand = brandRepository.findByManagerIdAndId(managerId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        // Delete image from S3 if exists
        brand.s3Key?.let { s3Key ->
            try {
                s3StorageService.deleteFile(s3Key)
            } catch (e: Exception) {
                // Log but don't fail if deletion fails
                println("Warning: Failed to delete brand image: ${e.message}")
            }
        }

        // Remove brand from all products that use this brand
        productService.removeBrandFromProducts(managerId, brandId)

        brandRepository.deleteById(brandId)
    }
}
