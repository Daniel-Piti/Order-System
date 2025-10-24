package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductOverrideRepository : JpaRepository<ProductOverrideDbEntity, String> {
  fun findByUserId(userId: String): List<ProductOverrideDbEntity>
  fun findByUserIdAndId(userId: String, id: String): ProductOverrideDbEntity?
  fun findByProductIdAndCustomerId(productId: String, customerId: String): ProductOverrideDbEntity?
  fun findByUserIdAndProductId(userId: String, productId: String): List<ProductOverrideDbEntity>
  fun findByUserIdAndCustomerId(userId: String, customerId: String): List<ProductOverrideDbEntity>
  
  // Paginated queries with JOIN to get product prices
  @Query("""
    SELECT po.id, po.product_id, po.user_id, po.customer_id, po.override_price, p.special_price as original_price
    FROM product_overrides po
    JOIN products p ON po.product_id = p.id
    WHERE po.user_id = :userId
    AND (:productId IS NULL OR po.product_id = :productId)
    AND (:customerId IS NULL OR po.customer_id = :customerId)
  """,
  countQuery = """
    SELECT COUNT(*)
    FROM product_overrides po
    JOIN products p ON po.product_id = p.id
    WHERE po.user_id = :userId
    AND (:productId IS NULL OR po.product_id = :productId)
    AND (:customerId IS NULL OR po.customer_id = :customerId)
  """,
  nativeQuery = true)
  fun findOverridesWithPrice(
    @Param("userId") userId: String,
    @Param("productId") productId: String?,
    @Param("customerId") customerId: String?,
    pageable: Pageable
  ): Page<Array<Any>>
}

