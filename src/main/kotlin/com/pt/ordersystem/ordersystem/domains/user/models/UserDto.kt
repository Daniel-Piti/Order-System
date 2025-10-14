package com.pt.ordersystem.ordersystem.domains.user.models

import java.time.LocalDate

data class UserDto(
  val firstName: String,
  val lastName: String,
  val email: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val mainAddress: String,
)

fun UserDbEntity.toDto(): UserDto = UserDto(
  firstName = this.firstName,
  lastName = this.lastName,
  email = this.email,
  phoneNumber = this.phoneNumber,
  dateOfBirth = this.dateOfBirth,
  mainAddress = this.mainAddress,
)