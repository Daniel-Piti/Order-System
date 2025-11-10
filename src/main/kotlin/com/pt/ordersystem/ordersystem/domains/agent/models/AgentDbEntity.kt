package com.pt.ordersystem.ordersystem.domains.agent.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "agents")
data class AgentDbEntity(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,

  @Column(name = "manager_id", nullable = false, length = 255)
  val managerId: String,

  @Column(name = "first_name", nullable = false, length = 255)
  val firstName: String,

  @Column(name = "last_name", nullable = false, length = 255)
  val lastName: String,

  @Column(nullable = false, unique = true, length = 255)
  val email: String,

  @Column(nullable = false, length = 255)
  var password: String,

  @Column(name = "phone_number", nullable = false, length = 20)
  val phoneNumber: String,

  @Column(name = "street_address", nullable = false, length = 255)
  val streetAddress: String,

  @Column(nullable = false, length = 100)
  val city: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime = LocalDateTime.now()
)
