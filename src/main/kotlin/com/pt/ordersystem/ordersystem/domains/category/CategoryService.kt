package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.helpers.CategoryValidators
import com.pt.ordersystem.ordersystem.domains.category.models.Category
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDbEntity
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryFailureReason
import com.pt.ordersystem.ordersystem.domains.category.models.CreateCategoryRequest
import com.pt.ordersystem.ordersystem.domains.category.models.UpdateCategoryRequest
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
    private val productService: ProductService,
) {

    fun getManagerCategories(managerId: String): List<Category> =
        categoryRepository.findByManagerId(managerId)

    fun validateCreateCategory(categoryName: String, managerId: String) {
        FieldValidators.validateNonEmpty(categoryName, "'category'")

        CategoryValidators.validateCategoriesCount(categoryRepository.countByManagerId(managerId), managerId)

        if (categoryRepository.existsByManagerIdAndCategory(managerId, categoryName)) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, category=$categoryName",
                severity = SeverityLevel.INFO,
            )
        }
    }

    @Transactional
    fun createCategory(managerId: String, request: CreateCategoryRequest): Category {
        validateCreateCategory(request.category, managerId)

        val now = LocalDateTime.now()
        val entity = CategoryDbEntity(
            managerId = managerId,
            category = request.category,
            createdAt = now,
            updatedAt = now,
        )

        return categoryRepository.save(entity)
    }

    fun validateUpdateCategory(
        managerId: String,
        categoryId: Long,
        newCategoryName: String,
    ) {
        FieldValidators.validateNonEmpty(newCategoryName, "'category'")

        if (categoryRepository.hasDuplicateCategory(managerId, newCategoryName, categoryId)) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = CategoryFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = CategoryFailureReason.ALREADY_EXISTS.technical + "managerId=$managerId, category=$newCategoryName",
                severity = SeverityLevel.INFO,
            )
        }
    }

    @Transactional
    fun updateCategory(managerId: String, categoryId: Long, request: UpdateCategoryRequest): Category {
        val category = categoryRepository.findByManagerIdAndId(managerId, categoryId)

        validateUpdateCategory(
            managerId = managerId,
            categoryId = categoryId,
            newCategoryName = request.category,
        )

        val updatedEntity = CategoryDbEntity(
            id = category.id,
            managerId = category.managerId,
            category = request.category,
            createdAt = category.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        return categoryRepository.save(updatedEntity)
    }

    @Transactional
    fun deleteCategory(managerId: String, categoryId: Long) {
        categoryRepository.findByManagerIdAndId(managerId, categoryId)

        productService.removeCategoryFromProducts(managerId, categoryId)

        categoryRepository.deleteById(categoryId)
    }
}
