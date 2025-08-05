package com.pt.ordersystem.ordersystem.user.models

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class UserLoginRequest(
  @field:Schema(example = "@gmail.com")
  val email: String,

  @field:Schema(example = "Aa123456!")
  val password: String
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
