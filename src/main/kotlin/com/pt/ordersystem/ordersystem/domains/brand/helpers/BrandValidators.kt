package com.pt.ordersystem.ordersystem.domains.brand.helpers

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandFailureReason
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus

object BrandValidators {

    private const val MAX_BRANDS_PER_MANAGER = 100

    fun validateBrandLimit(currentCount: Long, managerId: String) {
        if (currentCount >= MAX_BRANDS_PER_MANAGER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = BrandFailureReason.BRAND_LIMIT_EXCEEDED.userMessage,
                technicalMessage = BrandFailureReason.BRAND_LIMIT_EXCEEDED.technical + "managerId=$managerId, limit=$MAX_BRANDS_PER_MANAGER",
                severity = SeverityLevel.WARN
            )
        }
    }

    fun validateNameNotDuplicate(exists: Boolean, managerId: String, name: String) {
        if (exists) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = BrandFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = BrandFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, name=$name",
                severity = SeverityLevel.INFO
            )
        }
    }

    fun validateCreateBrand(
        brandName: String,
        managerId: String,
        brandsCount: Long,
        brandAlreadyExists: Boolean,
    ) {
        FieldValidators.validateNonEmpty(brandName, "'name'")

        validateBrandLimit(brandsCount, managerId)

        validateNameNotDuplicate(brandAlreadyExists, managerId, brandName)
    }

    fun validateUpdateBrand(
        brandName: String,
        managerId: String,
        brandAlreadyExists: Boolean,
    ) {
        FieldValidators.validateNonEmpty(brandName, "'name'")

        validateNameNotDuplicate(brandAlreadyExists, managerId, brandName)
    }
}
