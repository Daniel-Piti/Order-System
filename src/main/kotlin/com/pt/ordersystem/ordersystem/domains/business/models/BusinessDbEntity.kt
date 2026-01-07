package com.pt.ordersystem.ordersystem.domains.business.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "businesses")
data class BusinessDbEntity(
  @Id
  @Column(nullable = false, length = 255)
  val id: String,

  @Column(name = "manager_id", nullable = false, length = 255)
  val managerId: String,

  @Column(nullable = false)
  val name: String,

  @Column(name = "state_id_number", nullable = false)
  val stateIdNumber: String,

  @Column(nullable = false)
  val email: String,

  @Column(name = "phone_number", nullable = false)
  val phoneNumber: String,

  @Column(name = "street_address", nullable = false)
  val streetAddress: String,

  @Column(nullable = false)
  val city: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime = LocalDateTime.now()
)
