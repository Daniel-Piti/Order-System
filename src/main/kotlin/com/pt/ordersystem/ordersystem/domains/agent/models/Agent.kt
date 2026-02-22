package com.pt.ordersystem.ordersystem.domains.agent.models

import java.time.LocalDateTime

data class Agent(
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

fun AgentDbEntity.toModel(): Agent = Agent(
  id = this.id,
  managerId = this.managerId,
  firstName = this.firstName,
  lastName = this.lastName,
  email = this.email,
  phoneNumber = this.phoneNumber,
  streetAddress = this.streetAddress,
  city = this.city,
  createdAt = this.createdAt,
  updatedAt = this.updatedAt,
)

fun Agent.toDto(): AgentDto = AgentDto(
  id = this.id,
  managerId = this.managerId,
  firstName = this.firstName,
  lastName = this.lastName,
  email = this.email,
  phoneNumber = this.phoneNumber,
  streetAddress = this.streetAddress,
  city = this.city,
  createdAt = this.createdAt,
  updatedAt = this.updatedAt,
)
