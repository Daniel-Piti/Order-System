package com.pt.ordersystem.ordersystem.location.models

data class NewLocationRequest(
  val name: String,
  val address: String
)

data class UpdateLocationRequest(
  val name: String,
  val address: String
)