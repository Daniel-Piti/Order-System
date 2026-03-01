package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductOverride(
  val id: Long,
  val productId: String,
  val managerId: String,
  val agentId: String?,
  val customerId: String,
  val overridePrice: BigDecimal,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)

fun ProductOverrideDbEntity.toModel(): ProductOverride = ProductOverride(
  id = this.id,
  productId = this.productId,
  managerId = this.managerId,
  agentId = this.agentId,
  customerId = this.customerId,
  overridePrice = this.overridePrice,
  createdAt = this.createdAt,
  updatedAt = this.updatedAt,
)

fun ProductOverride.toDto(): ProductOverrideDto = ProductOverrideDto(
  id = id,
  productId = productId,
  managerId = managerId,
  agentId = agentId,
  customerId = customerId,
  overridePrice = overridePrice,
)

fun ProductOverride.toEntity(
  overridePrice: BigDecimal? = null,
  updatedAt: LocalDateTime? = null,
): ProductOverrideDbEntity = ProductOverrideDbEntity(
  id = id,
  productId = productId,
  managerId = managerId,
  agentId = agentId,
  customerId = customerId,
  overridePrice = overridePrice ?: this.overridePrice,
  createdAt = createdAt,
  updatedAt = updatedAt ?: this.updatedAt,
)
