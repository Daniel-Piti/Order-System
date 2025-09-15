package com.pt.ordersystem.ordersystem.customer.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "customers")
data class CustomerDbEntity(
  @Id
  val id: String,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(nullable = false)
  val name: String,

  @Column(name = "phone_number", nullable = false)
  val phoneNumber: String,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)

fun CustomerDbEntity.toDto() = CustomerDto(
  id = id,
  userId = userId,
  name = name,
  phoneNumber = phoneNumber,
)
