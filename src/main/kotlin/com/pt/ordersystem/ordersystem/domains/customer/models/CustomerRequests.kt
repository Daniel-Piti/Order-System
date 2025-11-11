package com.pt.ordersystem.ordersystem.domains.customer.models

data class CustomerPayload(
  val name: String,
  val phoneNumber: String,
  val email: String,
  val streetAddress: String,
  val city: String,
)
