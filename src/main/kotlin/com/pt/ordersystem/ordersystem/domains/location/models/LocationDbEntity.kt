package com.pt.ordersystem.ordersystem.domains.location.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "locations")
data class LocationDbEntity(

  @Id
  @Column(nullable = false)
  val id: String,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(nullable = false)
  val name: String,

  @Column(name = "street_address", nullable = false)
  val streetAddress: String,

  @Column(name = "city", nullable = false)
  val city: String,

  @Column(name = "phone_number", nullable = false)
  val phoneNumber: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime = LocalDateTime.now()
)
