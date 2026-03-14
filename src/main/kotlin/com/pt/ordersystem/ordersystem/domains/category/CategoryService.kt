package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.Category
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDbEntity
import com.pt.ordersystem.ordersystem.domains.category.models.CreateCategoryRequest
import com.pt.ordersystem.ordersystem.domains.category.models.UpdateCategoryRequest
import com.pt.ordersystem.ordersystem.domains.product.ProductService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productService: ProductService,
    private val categoryValidationService: CategoryValidationService,
) {

    fun getManagerCategories(managerId: String): List<Category> =
        categoryRepository.findByManagerId(managerId)

    @Transactional
    fun createCategory(managerId: String, request: CreateCategoryRequest): Category {
        categoryValidationService.validateCreateCategory(request.name, managerId)

        val now = LocalDateTime.now()
        val entity = CategoryDbEntity(
            managerId = managerId,
            name = request.name,
            createdAt = now,
            updatedAt = now,
        )

        return categoryRepository.save(entity)
    }

    @Transactional
    fun updateCategory(managerId: String, categoryId: Long, request: UpdateCategoryRequest): Category {
        val storedCategoryEntity = categoryRepository.findEntityByManagerIdAndId(managerId, categoryId)

        categoryValidationService.validateUpdateCategory(
            managerId = managerId,
            categoryId = categoryId,
            newCategoryName = request.name,
        )

        val updatedEntity = storedCategoryEntity.copy(
            name = request.name,
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
