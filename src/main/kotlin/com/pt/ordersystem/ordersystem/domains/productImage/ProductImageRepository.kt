package com.pt.ordersystem.ordersystem.domains.productImage

import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductImageRepository : JpaRepository<ProductImageDbEntity, Long> {
  fun findByProductId(productId: String): List<ProductImageDbEntity>
  fun findByManagerIdAndProductId(managerId: String, productId: String): List<ProductImageDbEntity>
  fun findByIdAndManagerId(id: Long, managerId: String): ProductImageDbEntity?
  fun deleteByProductId(productId: String)
}

