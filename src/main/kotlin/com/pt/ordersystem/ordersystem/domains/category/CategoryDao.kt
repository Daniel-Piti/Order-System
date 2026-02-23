package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryDao : JpaRepository<CategoryDbEntity, Long> {

    fun countByManagerId(managerId: String): Long

    fun findByManagerId(managerId: String): List<CategoryDbEntity>

    fun findByManagerIdAndId(managerId: String, id: Long): CategoryDbEntity?

    fun existsByManagerIdAndCategory(managerId: String, category: String): Boolean

    fun existsByManagerIdAndCategoryAndIdNot(managerId: String, category: String, id: Long): Boolean
}
