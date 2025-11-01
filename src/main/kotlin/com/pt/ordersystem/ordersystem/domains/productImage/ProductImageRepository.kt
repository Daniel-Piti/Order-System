package com.pt.ordersystem.ordersystem.domains.productImage

import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductImageRepository : JpaRepository<ProductImageDbEntity, Long> {
  fun findByProductId(productId: String): List<ProductImageDbEntity>
  fun findByUserIdAndProductId(userId: String, productId: String): List<ProductImageDbEntity>
  fun deleteByProductId(productId: String)
}

