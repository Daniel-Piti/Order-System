package com.pt.ordersystem.ordersystem.user

import java.time.LocalDate

data class UserDto(
  val firstName: String,
  val lastName: String,
  val email: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val mainAddress: String,
)

data class NewUserRequest(
  val firstName: String,
  val lastName: String,
  val email: String,
  val password: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val mainAddress: String,
)

data class UpdateUserRequest(
  val firstName: String,
  val lastName: String,
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