package com.pt.ordersystem.ordersystem.domains.manager.models

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ManagerLoginRequest(
  @field:Schema(example = "@gmail.com")
  val email: String,

  @field:Schema(example = "Aa123456!")
  val password: String
)

data class NewManagerRequest(
  val firstName: String,
  val lastName: String,
  val email: String,
  val businessName: String,
  val password: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val streetAddress: String,
  val city: String,
)

data class UpdateManagerDetailsRequest(
  val firstName: String,
  val lastName: String,
  val businessName: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val streetAddress: String,
  val city: String,
)

