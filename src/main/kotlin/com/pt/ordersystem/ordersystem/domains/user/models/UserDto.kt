package com.pt.ordersystem.ordersystem.domains.user.models

import java.time.LocalDate
import java.time.LocalDateTime

data class UserDto(
  val id: String,
  val firstName: String,
  val lastName: String,
  val email: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val mainAddress: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)

fun UserDbEntity.toDto(): UserDto = UserDto(
  id = this.id,
  firstName = this.firstName,
  lastName = this.lastName,
  email = this.email,
  phoneNumber = this.phoneNumber,
  dateOfBirth = this.dateOfBirth,
  mainAddress = this.mainAddress,
  createdAt = this.createdAt,
  updatedAt = this.updatedAt,
)