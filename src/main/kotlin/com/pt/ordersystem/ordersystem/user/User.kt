package com.pt.ordersystem.ordersystem.user

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(

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

  @Column(name = "main_address", nullable = false)
  val mainAddress: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime = LocalDateTime.now()
)
