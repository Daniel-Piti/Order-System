package com.pt.ordersystem.ordersystem.domains.customer.models

data class CustomerDto(
  val id: String,
  val agentId: Long?,
  val managerId: String,
  val name: String,
  val phoneNumber: String,
  val email: String,
  val streetAddress: String,
  val city: String,
)
