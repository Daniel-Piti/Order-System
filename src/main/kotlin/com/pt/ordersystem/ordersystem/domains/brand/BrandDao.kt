package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BrandDao : JpaRepository<BrandDbEntity, Long> {

    fun countByManagerId(managerId: String): Long

    fun findByManagerId(managerId: String): List<BrandDbEntity>

    fun findByManagerIdAndId(managerId: String, id: Long): BrandDbEntity?

    fun existsByManagerIdAndName(managerId: String, name: String): Boolean

    fun existsByManagerIdAndNameAndIdNot(managerId: String, name: String, id: Long): Boolean
}
