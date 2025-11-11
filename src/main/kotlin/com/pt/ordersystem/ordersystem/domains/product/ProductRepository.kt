package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ProductRepository : JpaRepository<ProductDbEntity, String> {
  fun findAllByManagerId(managerId: String): List<ProductDbEntity>
  fun findAllByManagerId(managerId: String, pageable: Pageable): Page<ProductDbEntity>
  fun findByManagerIdAndCategoryId(managerId: String, categoryId: Long, pageable: Pageable): Page<ProductDbEntity>
  fun findByManagerIdAndBrandId(managerId: String, brandId: Long, pageable: Pageable): Page<ProductDbEntity>
  fun findByManagerIdAndCategoryIdAndBrandId(managerId: String, categoryId: Long, brandId: Long, pageable: Pageable): Page<ProductDbEntity>
  
  @Modifying
  @Query("UPDATE ProductDbEntity p SET p.brandId = NULL, p.updatedAt = :updatedAt WHERE p.managerId = :managerId AND p.brandId = :brandId")
  fun removeBrandFromProducts(@Param("managerId") managerId: String, @Param("brandId") brandId: Long, @Param("updatedAt") updatedAt: LocalDateTime): Int
  
  @Modifying
  @Query("UPDATE ProductDbEntity p SET p.categoryId = NULL, p.updatedAt = :updatedAt WHERE p.managerId = :managerId AND p.categoryId = :categoryId")
  fun removeCategoryFromProducts(@Param("managerId") managerId: String, @Param("categoryId") categoryId: Long, @Param("updatedAt") updatedAt: LocalDateTime): Int
}