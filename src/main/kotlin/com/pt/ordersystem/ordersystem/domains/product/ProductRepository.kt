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
  fun findAllByUserId(userId: String): List<ProductDbEntity>
  fun findAllByUserId(userId: String, pageable: Pageable): Page<ProductDbEntity>
  fun findByUserIdAndCategoryId(userId: String, categoryId: Long): List<ProductDbEntity>
  fun findByUserIdAndCategoryId(userId: String, categoryId: Long, pageable: Pageable): Page<ProductDbEntity>
  fun findByUserIdAndBrandId(userId: String, brandId: Long): List<ProductDbEntity>
  
  @Modifying
  @Query("UPDATE ProductDbEntity p SET p.brandId = NULL, p.updatedAt = :updatedAt WHERE p.userId = :userId AND p.brandId = :brandId")
  fun removeBrandFromProducts(@Param("userId") userId: String, @Param("brandId") brandId: Long, @Param("updatedAt") updatedAt: LocalDateTime): Int
  
  @Modifying
  @Query("UPDATE ProductDbEntity p SET p.categoryId = NULL, p.updatedAt = :updatedAt WHERE p.userId = :userId AND p.categoryId = :categoryId")
  fun removeCategoryFromProducts(@Param("userId") userId: String, @Param("categoryId") categoryId: Long, @Param("updatedAt") updatedAt: LocalDateTime): Int
}