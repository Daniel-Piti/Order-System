package com.pt.ordersystem.ordersystem.domains.business.models

import java.time.LocalDateTime

data class BusinessDto(
  val id: String,
  val managerId: String,
  val name: String,
  val stateIdNumber: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)

fun BusinessDbEntity.toDto(): BusinessDto = BusinessDto(
  id = this.id,
  managerId = this.managerId,
  name = this.name,
  stateIdNumber = this.stateIdNumber,
  email = this.email,
  phoneNumber = this.phoneNumber,
  streetAddress = this.streetAddress,
  city = this.city,
  createdAt = this.createdAt,
  updatedAt = this.updatedAt,
)
