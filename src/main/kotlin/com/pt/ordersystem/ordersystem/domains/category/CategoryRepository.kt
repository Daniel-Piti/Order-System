package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryDbEntity, String> {
    
    fun findByUserId(userId: String): List<CategoryDbEntity>
    
    fun findByUserIdAndId(userId: String, id: String): CategoryDbEntity?
    
    fun findByUserIdAndCategory(userId: String, category: String): CategoryDbEntity?
    
    fun existsByUserIdAndCategory(userId: String, category: String): Boolean
}
