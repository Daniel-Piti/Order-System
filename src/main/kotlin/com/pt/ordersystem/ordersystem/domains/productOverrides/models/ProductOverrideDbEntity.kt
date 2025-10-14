package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "product_overrides")
data class ProductOverrideDbEntity(
  @Id
  val id: String,

  @Column(name = "product_id", nullable = false)
  val productId: String,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(name = "customer_id", nullable = false)
  val customerId: String,

  @Column(name = "override_price", nullable = false)
  val overridePrice: BigDecimal,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)

