package com.pt.ordersystem.ordersystem.domains.customer.models

data class CreateCustomerRequest(
  val name: String,
  val phoneNumber: String,
  val email: String,
  val streetAddress: String,
  val city: String,
)

data class UpdateCustomerRequest(
  val name: String,
  val phoneNumber: String,
  val email: String,
  val streetAddress: String,
  val city: String,
)

