package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface ProductOverrideRepository : JpaRepository<ProductOverrideDbEntity, Long> {
  fun findByManagerId(managerId: String): List<ProductOverrideDbEntity>
  fun findByManagerIdAndId(managerId: String, id: Long): ProductOverrideDbEntity?
  fun findByManagerIdAndProductId(managerId: String, productId: String): List<ProductOverrideDbEntity>
  fun findByManagerIdAndCustomerId(managerId: String, customerId: String): List<ProductOverrideDbEntity>
  fun findByManagerIdAndAgentIdIsNullAndProductIdAndCustomerId(managerId: String, productId: String, customerId: String): ProductOverrideDbEntity?
  fun findByManagerIdAndAgentIdAndProductIdAndCustomerId(managerId: String, agentId: Long, productId: String, customerId: String): ProductOverrideDbEntity?
  fun findByManagerIdAndAgentId(managerId: String, agentId: Long): List<ProductOverrideDbEntity>
  fun findByManagerIdAndAgentIdAndProductId(managerId: String, agentId: Long, productId: String): List<ProductOverrideDbEntity>
  fun findByManagerIdAndAgentIdAndCustomerId(managerId: String, agentId: Long, customerId: String): List<ProductOverrideDbEntity>

  @Modifying
  @Query("""
    UPDATE ProductOverrideDbEntity po 
    SET po.overridePrice = :newMinimumPrice, 
        po.updatedAt = :updatedAt 
    WHERE po.managerId = :managerId 
      AND po.productId = :productId 
      AND po.overridePrice < :newMinimumPrice
  """)
  fun updateInvalidOverridesForProduct(
    @Param("managerId") managerId: String,
    @Param("productId") productId: String,
    @Param("newMinimumPrice") newMinimumPrice: BigDecimal,
    @Param("updatedAt") updatedAt: LocalDateTime
  ): Int

  @Query("""
    SELECT po.id, po.product_id, po.manager_id, po.agent_id, po.customer_id, po.override_price, p.price as product_price, p.minimum_price as product_minimum_price
    FROM product_overrides po
    JOIN products p ON po.product_id = p.id
    WHERE po.manager_id = :managerId
    AND (:productId IS NULL OR po.product_id = :productId)
    AND (:customerId IS NULL OR po.customer_id = :customerId)
    AND (:agentId IS NULL OR po.agent_id = :agentId)
    AND (:includeManagerOverrides = true OR po.agent_id IS NOT NULL)
    AND (:includeAgentOverrides = true OR po.agent_id IS NULL)
  """,
    countQuery = """
    SELECT COUNT(*)
    FROM product_overrides po
    JOIN products p ON po.product_id = p.id
    WHERE po.manager_id = :managerId
    AND (:productId IS NULL OR po.product_id = :productId)
    AND (:customerId IS NULL OR po.customer_id = :customerId)
    AND (:agentId IS NULL OR po.agent_id = :agentId)
    AND (:includeManagerOverrides = true OR po.agent_id IS NOT NULL)
    AND (:includeAgentOverrides = true OR po.agent_id IS NULL)
  """,
    nativeQuery = true)
  fun findOverridesWithPrice(
    @Param("managerId") managerId: String,
    @Param("agentId") agentId: Long?,
    @Param("includeManagerOverrides") includeManagerOverrides: Boolean,
    @Param("includeAgentOverrides") includeAgentOverrides: Boolean,
    @Param("productId") productId: String?,
    @Param("customerId") customerId: String?,
    pageable: Pageable
  ): Page<Array<Any>>
}

