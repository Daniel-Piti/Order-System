package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.helpers.BrandValidators
import com.pt.ordersystem.ordersystem.domains.brand.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BrandService(
    private val brandRepository: BrandRepository,
    private val productService: ProductService,
    private val s3StorageService: S3StorageService
) {

    fun getBrandById(managerId: String, brandId: Long): Brand =
        brandRepository.findByManagerIdAndId(managerId, brandId)

    fun getManagerBrands(managerId: String): List<Brand> =
        brandRepository.findByManagerId(managerId)

    @Transactional
    fun createBrand(managerId: String, request: CreateBrandRequest): BrandCreateResponse {
        val trimmedBrandName = request.name.trim()
        val count = brandRepository.countByManagerId(managerId)
        val brandAlreadyExists = brandRepository.existsByManagerIdAndName(managerId, trimmedBrandName)

        BrandValidators.validateCreateBrand(
            brandName = trimmedBrandName,
            managerId = managerId,
            brandsCount = count,
            brandAlreadyExists = brandAlreadyExists,
        )

        // Handle image: generate presigned URL if image metadata provided
        val preSignedUrlResult = request.imageMetadata?.let { imageMetadata ->
            s3StorageService.generatePreSignedUploadUrl(
                basePath = "managers/$managerId/brands",
                imageMetadata = imageMetadata
            )
        }

        val now = LocalDateTime.now()
        val brand = BrandDbEntity(
            managerId = managerId,
            name = trimmedBrandName,
            s3Key = preSignedUrlResult?.s3Key,
            fileName = request.imageMetadata?.fileName,
            fileSizeBytes = request.imageMetadata?.fileSizeBytes,
            mimeType = request.imageMetadata?.contentType,
            createdAt = now,
            updatedAt = now
        )

        val brandId = brandRepository.save(brand)

        return BrandCreateResponse(
            brandId = brandId,
            preSignedUrl = preSignedUrlResult?.preSignedUrl
        )
    }

    @Transactional
    fun updateBrand(managerId: String, brandId: Long, request: UpdateBrandRequest): BrandUpdateResponse {
        val entity = brandRepository.findEntityByManagerIdAndId(managerId, brandId)

        val trimmedBrandName = request.name.trim()
        val brandAlreadyExists = brandRepository.hasDuplicateName(managerId, trimmedBrandName, brandId)

        BrandValidators.validateUpdateBrand(
            brandName = trimmedBrandName,
            managerId = managerId,
            brandAlreadyExists = brandAlreadyExists,
        )

        // Handle image: generate preSigned URL if image metadata provided
        val preSignedUrlResult = request.imageMetadata?.let { imageMetadata ->
            // Delete old image if exists
            entity.s3Key?.let { oldS3Key ->
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

        val updatedEntity = entity.copy(
            name = trimmedBrandName,
            s3Key = preSignedUrlResult?.s3Key ?: entity.s3Key,
            fileName = request.imageMetadata?.fileName ?: entity.fileName,
            fileSizeBytes = request.imageMetadata?.fileSizeBytes ?: entity.fileSizeBytes,
            mimeType = request.imageMetadata?.contentType ?: entity.mimeType,
            updatedAt = LocalDateTime.now()
        )

        val updatedBrandId = brandRepository.save(updatedEntity)

        return BrandUpdateResponse(
            brandId = updatedBrandId,
            preSignedUrl = preSignedUrlResult?.preSignedUrl
        )
    }

    @Transactional
    fun deleteBrand(managerId: String, brandId: Long) {
        val entity = brandRepository.findEntityByManagerIdAndId(managerId, brandId)

        // Delete image from S3 if exists
        entity.s3Key?.let { s3Key ->
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
