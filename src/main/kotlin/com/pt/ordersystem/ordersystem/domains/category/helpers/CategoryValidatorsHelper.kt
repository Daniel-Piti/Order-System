package com.pt.ordersystem.ordersystem.domains.category.helpers

import com.pt.ordersystem.ordersystem.domains.category.models.CategoryFailureReason
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus

object CategoryValidatorsHelper {

    private const val MAX_CATEGORIES_PER_MANAGER = 100

    fun validateCategoriesCount(categoriesCount: Long, managerId: String) {
        if (categoriesCount >= MAX_CATEGORIES_PER_MANAGER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = CategoryFailureReason.CATEGORY_LIMIT_EXCEEDED.userMessage,
                technicalMessage = CategoryFailureReason.CATEGORY_LIMIT_EXCEEDED.technical + "managerId=$managerId, limit=$MAX_CATEGORIES_PER_MANAGER",
                severity = SeverityLevel.WARN,
            )
        }
    }

    fun validateCategoryNameExists(exists: Boolean, categoryName: String, managerId: String) {
        if (exists) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, category=$categoryName",
                severity = SeverityLevel.INFO,
            )
        }
    }

    fun validateCategoryDuplication(hasDuplication: Boolean, categoryName: String, managerId: String) {
        if (hasDuplication)
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, category=$categoryName",
                severity = SeverityLevel.INFO,
            )
    }
}
