package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<ProductDbEntity, String> {
  fun findAllByUserId(userId: String): List<ProductDbEntity>
  fun findByUserIdAndId(userId: String, id: String): ProductDbEntity?
  fun findByUserIdAndCategoryId(userId: String, categoryId: String): List<ProductDbEntity>
}