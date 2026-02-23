package com.pt.ordersystem.ordersystem.domains.business.models

import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata

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

data class UpdateBusinessRequest(
  val name: String,
  val stateIdNumber: String,
  val email: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
  val imageMetadata: ImageMetadata? = null,
  val removeImage: Boolean? = null,
) {
  fun normalize(): UpdateBusinessRequest =
    this.copy(
      name = name.trim(),
      stateIdNumber = stateIdNumber.trim(),
      email = email.trim().lowercase(),
      phoneNumber = phoneNumber.trim(),
      streetAddress = streetAddress.trim(),
      city = city.trim(),
    )
}

data class BusinessUpdateResponse(
  val businessDto: BusinessDto,
  val preSignedUrl: String? = null,
)
