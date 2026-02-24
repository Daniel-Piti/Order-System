package com.pt.ordersystem.ordersystem.domains.customer.models

data class CustomerPayload(
  val name: String,
  val phoneNumber: String,
  val email: String,
  val streetAddress: String,
  val city: String,
  val stateId: String,
  val discountPercentage: Int = 0,
) {
  fun normalize(): CustomerPayload =
    this.copy(
      name = name.trim(),
      phoneNumber = phoneNumber.trim(),
      discountPercentage = discountPercentage.coerceIn(0, 100),
      email = email.trim().lowercase(),
      streetAddress = streetAddress.trim(),
      city = city.trim(),
      stateId = stateId.trim(),
    )
}
