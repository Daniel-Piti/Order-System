package com.pt.ordersystem.ordersystem.domains.manager.models

import java.time.LocalDate

data class NewManagerRequest(
  val firstName: String,
  val lastName: String,
  val email: String,
  val password: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val streetAddress: String,
  val city: String,
)

data class UpdateManagerDetailsRequest(
  val firstName: String,
  val lastName: String,
  val phoneNumber: String,
  val dateOfBirth: LocalDate,
  val streetAddress: String,
  val city: String,
)
