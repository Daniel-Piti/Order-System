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
        private const val MAX_CATEGORIES_PER_USER = 1000
    }

    fun getCategoryById(userId: String, categoryId: Long): CategoryDto {
        val category = categoryRepository.findByUserIdAndId(userId, categoryId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
                technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$categoryId",
                severity = SeverityLevel.WARN
            )

        return category.toDto()
    }

    fun getUserCategories(userId: String): List<CategoryDto> =
        categoryRepository.findByUserId(userId).map { it.toDto() }

    @Transactional
    fun createCategory(userId: String, request: CreateCategoryRequest): Long {
        with(request) {
            FieldValidators.validateNonEmpty(category, "'category'")
        }

        // Check if user has reached the maximum number of categories
        val existingCategoriesCount = categoryRepository.findByUserId(userId).size
        if (existingCategoriesCount >= MAX_CATEGORIES_PER_USER) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = CategoryFailureReason.CATEGORY_LIMIT_EXCEEDED.userMessage,
                technicalMessage = CategoryFailureReason.CATEGORY_LIMIT_EXCEEDED.technical + "userId=$userId, limit=$MAX_CATEGORIES_PER_USER",
                severity = SeverityLevel.WARN
            )
        }

        // Check if category already exists for this user
        if (categoryRepository.existsByUserIdAndCategory(userId, request.category.trim())) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "userId=$userId, category=${request.category}",
                severity = SeverityLevel.INFO
            )
        }

        val now = LocalDateTime.now()
        val category = CategoryDbEntity(
            userId = userId,
            category = request.category.trim(),
            createdAt = now,
            updatedAt = now
        )

        return categoryRepository.save(category).id
    }

    @Transactional
    fun updateCategory(userId: String, categoryId: Long, request: UpdateCategoryRequest): Long {
        val category = categoryRepository.findByUserIdAndId(userId, categoryId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
                technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$categoryId",
                severity = SeverityLevel.WARN
            )

        with(request) {
            FieldValidators.validateNonEmpty(this.category, "'category'")
        }

        // Check if new category name already exists for this user (excluding current category)
        val existingCategory = categoryRepository.findByUserIdAndCategory(userId, request.category.trim())
        if (existingCategory != null && existingCategory.id != categoryId) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "userId=$userId, category=${request.category}",
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
    fun deleteCategory(userId: String, categoryId: Long) {
        categoryRepository.findByUserIdAndId(userId, categoryId)
            ?: throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
                technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$categoryId",
                severity = SeverityLevel.WARN
            )

        // Remove category from all products that use this category
        productService.removeCategoryFromProducts(userId, categoryId)

        categoryRepository.deleteById(categoryId)
    }
}
