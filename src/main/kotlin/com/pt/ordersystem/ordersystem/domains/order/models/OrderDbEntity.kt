package com.pt.ordersystem.ordersystem.domains.order.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class OrderDbEntity(

  @Id
  val id: String,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(name = "customer_id")
  val customerId: String?,

  @Column(name = "customer_name")
  val customerName: String?,

  @Column(name = "customer_phone")
  val customerPhone: String?,

  @Column(name = "customer_email")
  val customerEmail: String?,

  @Column(name = "customer_city")
  val customerCity: String?,

  @Column(name = "customer_address")
  val customerAddress: String?,

  @Column(name = "location_id")
  val locationId: String?,

  @Column(nullable = false)
  val status: String,

  @Column(name = "products", columnDefinition = "json")
  val products: String? = null, // JSON string of product list

  @Column(name = "total_price", nullable = false)
  val totalPrice: BigDecimal,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)
