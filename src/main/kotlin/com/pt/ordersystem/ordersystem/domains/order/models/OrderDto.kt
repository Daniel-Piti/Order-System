package com.pt.ordersystem.ordersystem.domains.order.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.math.BigDecimal
import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.readValue

data class OrderDto(
  val id: String,
  val userId: String,
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val customerEmail: String,
  val customerCity: String?,
  val customerAddress: String?,
  val locationId: String?,
  val status: OrderStatus,
  val products: List<ProductDataForOrder>? = null,
  val totalPrice: BigDecimal,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
)

fun OrderDbEntity.toDto(): OrderDto {
  val mapper = jacksonObjectMapper()

  // Deserialize products JSON string to List<ProductDataForOrder> or null
  val productsJson = products
  val productsList: List<ProductDataForOrder>? = try {
    if (productsJson != null) mapper.readValue(productsJson) else null
  } catch (e: Exception) { null }

  // Convert status string to enum, fallback to a default if unknown
  val orderStatus = OrderStatus.valueOf(this.status)

  return OrderDto(
    id = this.id,
    userId = this.userId,
    customerId = this.customerId,
    customerName = this.customerName,
    customerPhone = this.customerPhone,
    customerEmail = this.customerEmail,
    customerCity = this.customerCity,
    customerAddress = this.customerAddress,
    locationId = this.locationId,
    status = orderStatus,
    products = productsList,
    totalPrice = this.totalPrice,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
  )
}
