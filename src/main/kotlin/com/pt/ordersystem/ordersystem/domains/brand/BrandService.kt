package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BrandService(
    private val brandRepository: BrandRepository,
    private val productService: ProductService
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

        return brand.toDto()
    }

    fun getUserBrands(userId: String): List<BrandDto> =
        brandRepository.findByUserId(userId).map { it.toDto() }

    @Transactional
    fun createBrand(userId: String, request: CreateBrandRequest): Long {
        with(request) {
            FieldValidators.validateNonEmpty(name, "'name'")
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
            s3Key = null,
            fileName = null,
            fileSizeBytes = null,
            mimeType = null,
            createdAt = now,
            updatedAt = now
        )

        return brandRepository.save(brand).id
    }

    @Transactional
    fun updateBrand(userId: String, brandId: Long, request: UpdateBrandRequest): Long {
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

        val updatedBrand = brand.copy(
            name = request.name.trim(),
            updatedAt = LocalDateTime.now()
        )

        return brandRepository.save(updatedBrand).id
    }

    @Transactional
    fun deleteBrand(userId: String, brandId: Long) {
        brandRepository.findByUserIdAndId(userId, brandId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = BrandFailureReason.NOT_FOUND.userMessage,
                technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$brandId",
                severity = SeverityLevel.WARN
            )

        // Remove brand from all products that use this brand
        productService.removeBrandFromProducts(userId, brandId)

        brandRepository.deleteById(brandId)
    }
}
