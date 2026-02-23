package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.Category
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDbEntity
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryFailureReason
import com.pt.ordersystem.ordersystem.domains.category.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class CategoryRepository(
    private val categoryDao: CategoryDao,
) {

    fun findByManagerIdAndId(managerId: String, id: Long): Category =
        categoryDao.findByManagerIdAndId(managerId, id)?.toModel() ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = CategoryFailureReason.NOT_FOUND.userMessage,
            technicalMessage = CategoryFailureReason.NOT_FOUND.technical + "categoryId=$id",
            severity = SeverityLevel.WARN,
        )

    fun findByManagerId(managerId: String): List<Category> =
        categoryDao.findByManagerId(managerId).map { it.toModel() }

    fun countByManagerId(managerId: String): Long =
        categoryDao.countByManagerId(managerId)

    fun existsByManagerIdAndCategory(managerId: String, category: String): Boolean =
        categoryDao.existsByManagerIdAndCategory(managerId, category)

    fun hasDuplicateCategory(managerId: String, category: String, id: Long): Boolean =
        categoryDao.existsByManagerIdAndCategoryAndIdNot(managerId, category, id)

    fun save(entity: CategoryDbEntity): Category = categoryDao.save(entity).toModel()

    fun deleteById(id: Long): Unit =
        categoryDao.deleteById(id)
}
