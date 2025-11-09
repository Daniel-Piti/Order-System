package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BrandRepository : JpaRepository<BrandDbEntity, Long> {
    
    fun findByManagerId(managerId: String): List<BrandDbEntity>
    
    fun findByManagerIdAndId(managerId: String, id: Long): BrandDbEntity?
    
    fun findByManagerIdAndName(managerId: String, name: String): BrandDbEntity?
    
    fun existsByManagerIdAndName(managerId: String, name: String): Boolean
}
