package com.pt.ordersystem.ordersystem.domains.agent.models

data class NewAgentRequest(
  val firstName: String,
  val lastName: String,
  val email: String,
  val password: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
)

data class UpdateAgentRequest(
  val firstName: String,
  val lastName: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
)
