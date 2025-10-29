package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.domains.product.models.ProductsJsonConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class OrderDbEntity(

  @Id
  val id: String,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  // User (Seller) pickup location - selected by customer
  @Column(name = "user_street_address")
  val userStreetAddress: String?,

  @Column(name = "user_city")
  val userCity: String?,

  @Column(name = "user_phone_number")
  val userPhoneNumber: String?,

  // Customer (Buyer) data
  @Column(name = "customer_id")
  val customerId: String?,

  @Column(name = "customer_name")
  val customerName: String?,

  @Column(name = "customer_phone")
  val customerPhone: String?,

  @Column(name = "customer_email")
  val customerEmail: String?,

  @Column(name = "customer_street_address")
  val customerStreetAddress: String?,

  @Column(name = "customer_city")
  val customerCity: String?,

  // Order details
  @Column(nullable = false)
  val status: String,

  @Column(name = "products", columnDefinition = "json", nullable = false)
  @Convert(converter = ProductsJsonConverter::class)
  val products: List<ProductDataForOrder>, // Automatically converted to/from JSON

  @Column(name = "products_version", nullable = false)
  val productsVersion: Int,

  @Column(name = "total_price", nullable = false)
  val totalPrice: BigDecimal,

  @Column(name = "delivery_date")
  val deliveryDate: LocalDate?,

  @Column(name = "link_expires_at", nullable = false)
  val linkExpiresAt: LocalDateTime,

  @Column(name = "notes", length = 2048, nullable = false)
  val notes: String = "",

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)
