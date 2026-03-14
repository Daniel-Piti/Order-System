package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.brand.BrandRepository
import com.pt.ordersystem.ordersystem.domains.category.CategoryRepository
import com.pt.ordersystem.ordersystem.domains.product.helpers.ProductValidators
import com.pt.ordersystem.ordersystem.domains.product.models.ProductInfo
import com.pt.ordersystem.ordersystem.domains.productImage.helpers.ProductImageValidators
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import org.springframework.stereotype.Service

@Service
class ProductValidationService(
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val s3StorageService: S3StorageService,
) {

    fun validateCreateProduct(
        managerId: String,
        productInfo: ProductInfo,
        imagesMetadata: List<ImageMetadata>,
    ) {
        validateProductBrandAndCategory(productInfo.brandId, productInfo.categoryId, managerId)

        // Check product limit
        val productCount = productRepository.countByManagerId(managerId)
        ProductValidators.validateMaxProductPerCustomer(productCount, managerId)

        // Image validations
        ProductImageValidators.validateMaxImagesForProduct(imagesMetadata.size)
        imagesMetadata.forEach { s3StorageService.validateImageMetadata(it) }
    }

    fun validateProductBrandAndCategory(brandId: Long?, categoryId: Long?, managerId: String) {
        // Validate brandId belongs to manager (if provided)
        brandId?.let { brandRepository.findByManagerIdAndId(managerId, it) }

        // Validate categoryId belongs to manager (if provided)
        categoryId?.let { categoryRepository.findByManagerIdAndId(managerId, it) }
    }

}
