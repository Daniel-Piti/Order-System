package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class OrderDto(
  val id: String,
  val userId: String,
  
  // User location (selected by customer)
  val userStreetAddress: String?,
  val userCity: String?,
  val userPhoneNumber: String?,
  
  // Customer data
  val customerId: String?,
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
  val deliveryDate: LocalDate?,
  val linkExpiresAt: LocalDateTime,
  val notes: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
)

fun OrderDbEntity.toDto(): OrderDto {
  // Convert status string to enum
  val orderStatus = OrderStatus.valueOf(this.status)

  return OrderDto(
    id = this.id,
    userId = this.userId,
    userStreetAddress = this.userStreetAddress,
    userCity = this.userCity,
    userPhoneNumber = this.userPhoneNumber,
    customerId = this.customerId,
    customerName = this.customerName,
    customerPhone = this.customerPhone,
    customerEmail = this.customerEmail,
    customerStreetAddress = this.customerStreetAddress,
    customerCity = this.customerCity,
    status = orderStatus,
    products = this.products, // Already a List<ProductDataForOrder> - JPA converter handles it!
    productsVersion = this.productsVersion,
    totalPrice = this.totalPrice,
    deliveryDate = this.deliveryDate,
    linkExpiresAt = this.linkExpiresAt,
    notes = this.notes,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
  )
}
