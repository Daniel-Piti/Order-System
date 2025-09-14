package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.product.models.ProductOverrideDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductOverrideRepository : JpaRepository<ProductOverrideDbEntity, String> {
  fun findByUserId(userId: String): List<ProductOverrideDbEntity>
  fun findByUserIdAndId(userId: String, id: String): ProductOverrideDbEntity?
  fun findByProductIdAndCustomerId(productId: String, customerId: String): ProductOverrideDbEntity?
  fun findByUserIdAndProductId(userId: String, productId: String): List<ProductOverrideDbEntity>
}

