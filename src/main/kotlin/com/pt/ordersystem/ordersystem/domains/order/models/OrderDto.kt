package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDto(
  val id: String,
  val referenceId: Long,
  val orderSource: OrderSource,
  val managerId: String,
  val agentId: String?,
  val customerId: String?,

  val storeStreetAddress: String?,
  val storeCity: String?,
  val storePhoneNumber: String?,

  val customerName: String?,
  val customerPhone: String?,
  val customerEmail: String?,
  val customerStreetAddress: String?,
  val customerCity: String?,
  val customerStateId: String?,

  val status: OrderStatus,
  val products: List<ProductDataForOrder>,
  val productsVersion: Int,
  val totalPrice: BigDecimal,
  val discount: BigDecimal,
  val vat: BigDecimal,
  val linkExpiresAt: LocalDateTime,
  val notes: String,
  val placedAt: LocalDateTime?,
  val doneAt: LocalDateTime?,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)
