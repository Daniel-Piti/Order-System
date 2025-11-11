package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "product_overrides")
data class ProductOverrideDbEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,

  @Column(name = "product_id", nullable = false)
  val productId: String,

  @Column(name = "manager_id", nullable = false)
  val managerId: String,

  @Column(name = "agent_id")
  val agentId: Long? = null,

  @Column(name = "customer_id", nullable = false)
  val customerId: String,

  @Column(name = "override_price", nullable = false)
  val overridePrice: BigDecimal,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)

