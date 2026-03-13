package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.helpers.BrandValidatorsHelper
import com.pt.ordersystem.ordersystem.domains.brand.models.CreateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandRequest
import org.springframework.stereotype.Service

@Service
class BrandValidationService(
    private val brandRepository: BrandRepository,
) {

    fun validateUpdateBrand(managerId: String, request: UpdateBrandRequest, storedBrandName: String) {
        val brandNameAlreadyExists = brandRepository.existsByManagerIdAndName(managerId, request.name)
        BrandValidatorsHelper.validateNameNotDuplicate(brandNameAlreadyExists, managerId, request.name)
    }

    fun validateCreateBrand(managerId: String, request: CreateBrandRequest) {
        val brandsCount = brandRepository.countByManagerId(managerId)
        val brandNameAlreadyExists = brandRepository.existsByManagerIdAndName(managerId, request.name)

        BrandValidatorsHelper.validateBrandsLimit(brandsCount, managerId)
        BrandValidatorsHelper.validateNameNotDuplicate(brandNameAlreadyExists, managerId, request.name)
    }
}