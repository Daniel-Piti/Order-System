package com.pt.ordersystem.ordersystem.domains.agent.models

import java.time.LocalDateTime

data class AgentDto(
  val id: String,
  val managerId: String,
  val firstName: String,
  val lastName: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)
