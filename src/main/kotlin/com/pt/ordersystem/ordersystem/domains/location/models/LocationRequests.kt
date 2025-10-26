package com.pt.ordersystem.ordersystem.domains.location.models

data class NewLocationRequest(
  val name: String,
  val streetAddress: String,
  val city: String,
  val phoneNumber: String,
)

data class UpdateLocationRequest(
  val name: String,
  val streetAddress: String,
  val city: String,
  val phoneNumber: String,
)