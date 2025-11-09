package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryDbEntity, Long> {
    
    fun findByManagerId(managerId: String): List<CategoryDbEntity>
    
    fun findByManagerIdAndId(managerId: String, id: Long): CategoryDbEntity?
    
    fun findByManagerIdAndCategory(managerId: String, category: String): CategoryDbEntity?
    
    fun existsByManagerIdAndCategory(managerId: String, category: String): Boolean
}
