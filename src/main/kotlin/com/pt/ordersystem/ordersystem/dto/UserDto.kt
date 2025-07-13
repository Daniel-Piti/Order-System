package com.pt.ordersystem.ordersystem.dto

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

