package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.helpers.CategoryValidatorsHelper
import org.springframework.stereotype.Service

@Service
class CategoryValidationService(
    private val categoryRepository: CategoryRepository,
) {

    fun validateCreateCategory(categoryName: String, managerId: String) {

        CategoryValidatorsHelper.validateCategoriesCount(categoryRepository.countByManagerId(managerId), managerId)

        val categoryExists = categoryRepository.existsByManagerIdAndName(managerId, categoryName)

        CategoryValidatorsHelper.validateCategoryNameExists(categoryExists, categoryName, managerId)

    }

    fun validateUpdateCategory(managerId: String, categoryId: Long, newCategoryName: String) {
        val hasDuplication = categoryRepository.hasDuplicateCategory(managerId, newCategoryName, categoryId)
        CategoryValidatorsHelper.validateCategoryDuplication(hasDuplication, newCategoryName, managerId)
    }

}
