package com.pt.ordersystem.ordersystem.domains.customer.models

data class CreateCustomerRequest(
  val name: String,
  val phoneNumber: String
)

data class UpdateCustomerRequest(
  val name: String,
  val phoneNumber: String
)

