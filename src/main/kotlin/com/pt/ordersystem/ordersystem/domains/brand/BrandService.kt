package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.helpers.BrandValidators
import com.pt.ordersystem.ordersystem.domains.brand.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
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
    fun createBrand(managerId: String, request: CreateBrandRequest): CreateBrandResponse {
        val count = brandRepository.countByManagerId(managerId)
        val brandAlreadyExists = brandRepository.existsByManagerIdAndName(managerId, request.name)

        BrandValidators.validateCreateBrand(
            brandName = request.name,
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
            name = request.name,
            s3Key = preSignedUrlResult?.s3Key,
            fileName = request.imageMetadata?.fileName,
            fileSizeBytes = request.imageMetadata?.fileSizeBytes,
            mimeType = request.imageMetadata?.contentType,
            createdAt = now,
            updatedAt = now
        )

        val savedBrand = brandRepository.save(brand)
        return CreateBrandResponse(savedBrand.toDto(), preSignedUrlResult?.preSignedUrl)
    }

    @Transactional
    fun updateBrandName(managerId: String, brandId: Long, request: UpdateBrandRequest): Brand {
        val brand = brandRepository.findByManagerIdAndId(managerId, brandId)

        val brandAlreadyExists = brandRepository.hasDuplicateName(managerId, request.name, brandId)

        BrandValidators.validateUpdateBrand(
            brandName = request.name,
            managerId = managerId,
            brandAlreadyExists = brandAlreadyExists,
        )

        val updatedEntity = BrandDbEntity(
            id = brand.id,
            managerId = brand.managerId,
            name = request.name,
            s3Key = brand.s3Key,
            fileName = brand.fileName,
            fileSizeBytes = brand.fileSizeBytes,
            mimeType = brand.mimeType,
            createdAt = brand.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        return brandRepository.save(updatedEntity)
    }

    @Transactional
    fun removeBrandImage(managerId: String, brandId: Long) {
        val brand = brandRepository.findByManagerIdAndId(managerId, brandId)

        s3StorageService.deleteImageIfExists(brand.s3Key)

        val updatedEntity = BrandDbEntity(
            id = brand.id,
            managerId = brand.managerId,
            name = brand.name,
            s3Key = null,
            fileName = null,
            fileSizeBytes = null,
            mimeType = null,
            createdAt = brand.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        brandRepository.save(updatedEntity)
    }

    @Transactional
    fun setBrandImage(managerId: String, brandId: Long, imageMetadata: ImageMetadata): UpdateBrandImageResponse {
        val brand = brandRepository.findByManagerIdAndId(managerId, brandId)

        brand.s3Key?.let { s3StorageService.deleteImageIfExists(brand.s3Key) }

        val preSignedUrlResult = s3StorageService.generatePreSignedUploadUrl(
            basePath = "managers/$managerId/brands",
            imageMetadata = imageMetadata,
        )

        val updatedEntity = BrandDbEntity(
            id = brand.id,
            managerId = brand.managerId,
            name = brand.name,
            s3Key = preSignedUrlResult.s3Key,
            fileName = imageMetadata.fileName,
            fileSizeBytes = imageMetadata.fileSizeBytes,
            mimeType = imageMetadata.contentType,
            createdAt = brand.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        val updatedBrand = brandRepository.save(updatedEntity)
        return UpdateBrandImageResponse(updatedBrand.toDto(), preSignedUrlResult.preSignedUrl)
    }

    @Transactional
    fun deleteBrand(managerId: String, brandId: Long) {
        val entity = brandRepository.findByManagerIdAndId(managerId, brandId)

        // Delete image from S3 if exists
        entity.s3Key.let { s3StorageService.deleteImageIfExists(entity.s3Key) }

        // Remove brand from all products that use this brand
        productService.removeBrandFromProducts(managerId, brandId)

        brandRepository.deleteById(brandId)
    }
}
