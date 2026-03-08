package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDbEntity
import com.pt.ordersystem.ordersystem.domains.product.models.ProductMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ProductDao : JpaRepository<ProductDbEntity, String> {

  @Query(
    value = """
      SELECT p.id AS id, p.manager_id AS managerId, p.name AS name,
             p.brand_id AS brandId, b.name AS brandName,
             p.category_id AS categoryId, c.category AS categoryName,
             p.minimum_price AS minimumPrice, p.price AS price, p.description AS description
      FROM products p
      LEFT JOIN brands b ON p.brand_id = b.id AND p.manager_id = b.manager_id
      LEFT JOIN categories c ON p.category_id = c.id AND p.manager_id = c.manager_id
      WHERE p.manager_id = :managerId
    """,
    nativeQuery = true
  )
  fun findAllProductsByManagerId(@Param("managerId") managerId: String): List<ProductMapper>

  fun countByManagerId(managerId: String): Long
  fun findByManagerIdAndId(managerId: String, id: String): ProductDbEntity?

  @Modifying
  @Query("UPDATE ProductDbEntity p SET p.brandId = NULL, p.updatedAt = :updatedAt WHERE p.managerId = :managerId AND p.brandId = :brandId")
  fun removeBrandFromProducts(@Param("managerId") managerId: String, @Param("brandId") brandId: Long, @Param("updatedAt") updatedAt: LocalDateTime): Int

  @Modifying
  @Query("UPDATE ProductDbEntity p SET p.categoryId = NULL, p.updatedAt = :updatedAt WHERE p.managerId = :managerId AND p.categoryId = :categoryId")
  fun removeCategoryFromProducts(@Param("managerId") managerId: String, @Param("categoryId") categoryId: Long, @Param("updatedAt") updatedAt: LocalDateTime): Int
}
