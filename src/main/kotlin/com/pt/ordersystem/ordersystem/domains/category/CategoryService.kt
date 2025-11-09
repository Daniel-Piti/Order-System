package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productService: ProductService
) {

    companion object {
        private const val MAX_CATEGORIES_PER_MANAGER = 1000
    }

    fun getCategoryById(managerId: String, categoryId: Long): CategoryDto {
        val category = categoryRepository.findByManagerIdAndId(managerId, categoryId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
                technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$categoryId",
                severity = SeverityLevel.WARN
            )

        return category.toDto()
    }

    fun getManagerCategories(managerId: String): List<CategoryDto> =
        categoryRepository.findByManagerId(managerId).map { it.toDto() }

    @Transactional
    fun createCategory(managerId: String, request: CreateCategoryRequest): Long {
        with(request) {
            FieldValidators.validateNonEmpty(category, "'category'")
        }

        // Check if manager has reached the maximum number of categories
        val existingCategoriesCount = categoryRepository.findByManagerId(managerId).size
        if (existingCategoriesCount >= MAX_CATEGORIES_PER_MANAGER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = CategoryFailureReason.CATEGORY_LIMIT_EXCEEDED.userMessage,
                technicalMessage = CategoryFailureReason.CATEGORY_LIMIT_EXCEEDED.technical + "managerId=$managerId, limit=$MAX_CATEGORIES_PER_MANAGER",
                severity = SeverityLevel.WARN
            )
        }

        // Check if category already exists for this manager
        if (categoryRepository.existsByManagerIdAndCategory(managerId, request.category.trim())) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, category=${request.category}",
                severity = SeverityLevel.INFO
            )
        }

        val now = LocalDateTime.now()
        val category = CategoryDbEntity(
            managerId = managerId,
            category = request.category.trim(),
            createdAt = now,
            updatedAt = now
        )

        return categoryRepository.save(category).id
    }

    @Transactional
    fun updateCategory(managerId: String, categoryId: Long, request: UpdateCategoryRequest): Long {
        val category = categoryRepository.findByManagerIdAndId(managerId, categoryId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
                technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$categoryId",
                severity = SeverityLevel.WARN
            )

        with(request) {
            FieldValidators.validateNonEmpty(this.category, "'category'")
        }

        // Check if new category name already exists for this manager (excluding current category)
        val existingCategory = categoryRepository.findByManagerIdAndCategory(managerId, request.category.trim())
        if (existingCategory != null && existingCategory.id != categoryId) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, category=${request.category}",
                severity = SeverityLevel.INFO
            )
        }

        val updatedCategory = category.copy(
            category = request.category.trim(),
            updatedAt = LocalDateTime.now()
        )

        return categoryRepository.save(updatedCategory).id
    }

    @Transactional
    fun deleteCategory(managerId: String, categoryId: Long) {
        categoryRepository.findByManagerIdAndId(managerId, categoryId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
                technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$categoryId",
                severity = SeverityLevel.WARN
            )

        // Remove category from all products that use this category
        productService.removeCategoryFromProducts(managerId, categoryId)

        categoryRepository.deleteById(categoryId)
    }
}
