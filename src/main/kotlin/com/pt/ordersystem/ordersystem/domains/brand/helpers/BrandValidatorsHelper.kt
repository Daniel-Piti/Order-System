package com.pt.ordersystem.ordersystem.domains.brand.helpers

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandFailureReason
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus

object BrandValidatorsHelper {

    private const val MAX_BRANDS_PER_MANAGER = 100

    fun validateBrandsLimit(currentCount: Long, managerId: String) {
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

}
