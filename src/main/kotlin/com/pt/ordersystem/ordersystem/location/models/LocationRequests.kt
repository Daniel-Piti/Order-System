package com.pt.ordersystem.ordersystem.location.models

data class NewLocationRequest(
  val name: String,
  val address: String,
  val phoneNumber: String,
)

data class UpdateLocationRequest(
  val name: String,
  val address: String,
  val phoneNumber: String,
)