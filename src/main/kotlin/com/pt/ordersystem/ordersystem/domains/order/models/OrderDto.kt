package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDto(
  val id: String,
  val orderSource: OrderSource,
  val managerId: String,
  val agentId: Long?,
  val customerId: String?,
  
  // Store location (selected by customer)
  val storeStreetAddress: String?,
  val storeCity: String?,
  val storePhoneNumber: String?,
  
  // Customer data
  val customerName: String?,
  val customerPhone: String?,
  val customerEmail: String?,
  val customerStreetAddress: String?,
  val customerCity: String?,
  
  // Order details
  val status: OrderStatus,
  val products: List<ProductDataForOrder>,
  val productsVersion: Int,
  val totalPrice: BigDecimal,
  val linkExpiresAt: LocalDateTime,
  val notes: String,
  val placedAt: LocalDateTime?,
  val doneAt: LocalDateTime?,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
)

fun OrderDbEntity.toDto(): OrderDto {
  // Convert status string to enum
  val orderStatus = OrderStatus.valueOf(this.status)
  val orderSourceEnum = OrderSource.valueOf(this.orderSource)

  return OrderDto(
    id = this.id,
    orderSource = orderSourceEnum,
    managerId = this.managerId,
    agentId = this.agentId,
    customerId = this.customerId,
    storeStreetAddress = this.storeStreetAddress,
    storeCity = this.storeCity,
    storePhoneNumber = this.storePhoneNumber,
    customerName = this.customerName,
    customerPhone = this.customerPhone,
    customerEmail = this.customerEmail,
    customerStreetAddress = this.customerStreetAddress,
    customerCity = this.customerCity,
    status = orderStatus,
    products = this.products, // Already a List<ProductDataForOrder> - JPA converter handles it!
    productsVersion = this.productsVersion,
    totalPrice = this.totalPrice,
    linkExpiresAt = this.linkExpiresAt,
    notes = this.notes,
    placedAt = this.placedAt,
    doneAt = this.doneAt,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
  )
}
