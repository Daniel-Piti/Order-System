package com.pt.ordersystem.ordersystem.domains.order.models

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.domains.product.models.ProductsJsonConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
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

  @Column(name = "order_source", nullable = false)
  val orderSource: String,

  @Column(name = "manager_id", nullable = false)
  val managerId: String,

  @Column(name = "agent_id")
  val agentId: Long? = null,

  @Column(name = "customer_id")
  val customerId: String?,

  // Store (pickup location) - selected by customer
  @Column(name = "store_street_address")
  val storeStreetAddress: String?,

  @Column(name = "store_city")
  val storeCity: String?,

  @Column(name = "store_phone_number")
  val storePhoneNumber: String?,

  // Customer (Buyer) data

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

  @Column(name = "customer_state_id")
  val customerStateId: String?,

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

  @Column(name = "discount", nullable = false)
  val discount: BigDecimal = BigDecimal.ZERO,

  @Column(name = "link_expires_at", nullable = false)
  val linkExpiresAt: LocalDateTime,

  @Column(name = "notes", length = 2048, nullable = false)
  val notes: String = "",

  @Column(name = "placed_at")
  val placedAt: LocalDateTime? = null,

  @Column(name = "done_at")
  val doneAt: LocalDateTime? = null,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)
