package com.pt.ordersystem.ordersystem.domains.manager.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "managers")
data class ManagerDbEntity(

  @Id
  @Column(nullable = false, length = 255)
  val id: String,

  @Column(name = "first_name", nullable = false)
  val firstName: String,

  @Column(name = "last_name", nullable = false)
  val lastName: String,

  @Column(nullable = false, unique = true)
  val email: String,

  @Column(nullable = false)
  var password: String,

  @Column(nullable = false)
  val phoneNumber: String,

  @Column(name = "date_of_birth", nullable = false)
  val dateOfBirth: LocalDate,

  @Column(name = "street_address", nullable = false)
  val streetAddress: String,

  @Column(name = "city", nullable = false)
  val city: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime = LocalDateTime.now()
)

