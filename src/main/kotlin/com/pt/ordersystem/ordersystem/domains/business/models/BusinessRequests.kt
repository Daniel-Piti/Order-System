package com.pt.ordersystem.ordersystem.domains.business.models

data class CreateBusinessRequest(
  val managerId: String,
  val name: String,
  val stateIdNumber: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
)

data class UpdateBusinessRequest(
  val name: String,
  val stateIdNumber: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
)
