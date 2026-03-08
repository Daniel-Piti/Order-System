package com.pt.ordersystem.ordersystem.domains.product.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class ProductDbEntity(
  @Id
  val id: String,

  @Column(name = "manager_id", nullable = false)
  val managerId: String,

  @Column(nullable = false)
  val name: String,

  @Column(name = "brand_id", nullable = true)
  val brandId: Long?,

  @Column(name = "category_id", nullable = true)
  val categoryId: Long?,

  @Column(name = "minimum_price", nullable = false)
  val minimumPrice: BigDecimal,

  @Column(name = "price", nullable = false)
  val price: BigDecimal,

  @Column(columnDefinition = "TEXT", nullable = false)
  val description: String,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now(),
)
