package com.pt.ordersystem.ordersystem.customer.models

data class CreateCustomerRequest(
  val name: String,
  val phoneNumber: String
)

data class UpdateCustomerRequest(
  val name: String,
  val phoneNumber: String
)

