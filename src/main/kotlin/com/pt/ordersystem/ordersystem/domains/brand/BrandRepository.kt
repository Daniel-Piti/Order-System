package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BrandRepository : JpaRepository<BrandDbEntity, Long> {
    
    fun findByUserId(userId: String): List<BrandDbEntity>
    
    fun findByUserIdAndId(userId: String, id: Long): BrandDbEntity?
    
    fun findByUserIdAndName(userId: String, name: String): BrandDbEntity?
    
    fun existsByUserIdAndName(userId: String, name: String): Boolean
}
