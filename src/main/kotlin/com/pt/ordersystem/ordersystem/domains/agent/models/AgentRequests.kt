package com.pt.ordersystem.ordersystem.domains.agent.models

data class NewAgentRequest(
  val firstName: String,
  val lastName: String,
  val email: String,
  val password: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
) {
  fun normalize() = this.copy(
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    email = email.trim().lowercase(),
    phoneNumber = phoneNumber.trim(),
    streetAddress = streetAddress.trim(),
    city = city.trim(),
  )
}

data class UpdateAgentRequest(
  val firstName: String,
  val lastName: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
) {
  fun normalize() = this.copy(
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    phoneNumber = phoneNumber.trim(),
    streetAddress = streetAddress.trim(),
    city = city.trim(),
  )
}
