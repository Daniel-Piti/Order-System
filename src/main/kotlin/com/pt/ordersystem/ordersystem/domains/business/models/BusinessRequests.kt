package com.pt.ordersystem.ordersystem.domains.business.models

data class CreateBusinessRequest(
  val managerId: String,
  val name: String,
  val stateIdNumber: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
) {
  fun normalize(): CreateBusinessRequest =
    this.copy(
      name = name.trim(),
      stateIdNumber = stateIdNumber.trim(),
      email = email.trim().lowercase(),
      phoneNumber = phoneNumber.trim(),
      streetAddress = streetAddress.trim(),
      city = city.trim(),
    )
}

data class UpdateBusinessDetailsRequest(
  val name: String,
  val stateIdNumber: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
) {
  fun normalize(): UpdateBusinessDetailsRequest =
    this.copy(
      name = name.trim(),
      stateIdNumber = stateIdNumber.trim(),
      email = email.trim().lowercase(),
      phoneNumber = phoneNumber.trim(),
      streetAddress = streetAddress.trim(),
      city = city.trim(),
    )
}

data class UpdateBusinessDetailsResponse(
  val business: BusinessDto,
)

data class SetBusinessImageResponse(
  val business: BusinessDto,
  val preSignedUrl: String,
)
