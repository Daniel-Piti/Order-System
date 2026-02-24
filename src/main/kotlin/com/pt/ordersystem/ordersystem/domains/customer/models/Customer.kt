package com.pt.ordersystem.ordersystem.domains.customer.models

import java.time.LocalDateTime

data class Customer(
  val id: String,
  val agentId: String?,
  val managerId: String,
  val discountPercentage: Int,
  val name: String,
  val phoneNumber: String,
  val email: String,
  val streetAddress: String,
  val city: String,
  val stateId: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)

fun CustomerDbEntity.toModel(): Customer = Customer(
  id = id,
  agentId = agentId,
  managerId = managerId,
  discountPercentage = discountPercentage,
  name = name,
  phoneNumber = phoneNumber,
  email = email,
  streetAddress = streetAddress,
  city = city,
  stateId = stateId,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Customer.toDto(): CustomerDto = CustomerDto(
  id = id,
  agentId = agentId,
  managerId = managerId,
  discountPercentage = discountPercentage,
  name = name,
  phoneNumber = phoneNumber,
  email = email,
  streetAddress = streetAddress,
  city = city,
  stateId = stateId,
)

fun Customer.toDbEntity(): CustomerDbEntity = CustomerDbEntity(
  id = id,
  agentId = agentId,
  managerId = managerId,
  discountPercentage = discountPercentage,
  name = name,
  phoneNumber = phoneNumber,
  email = email,
  streetAddress = streetAddress,
  city = city,
  stateId = stateId,
  createdAt = createdAt,
  updatedAt = updatedAt,
)
