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

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(nullable = false)
  val name: String,

  @Column(nullable = true)
  val category: String?,

  @Column(name = "original_price", nullable = false)
  val originalPrice: BigDecimal,

  @Column(name = "special_price", nullable = false)
  val specialPrice: BigDecimal,

  @Column(name = "picture_url")
  val pictureUrl: String,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now(),
)
