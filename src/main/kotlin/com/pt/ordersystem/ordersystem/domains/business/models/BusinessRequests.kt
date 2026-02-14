package com.pt.ordersystem.ordersystem.domains.business.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata

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
  val imageMetadata: ImageMetadata? = null,
  @JsonProperty("removeImage") val removeImage: Boolean? = null,
)

data class BusinessUpdateResponse(
  val businessId: String,
  val preSignedUrl: String? = null,
)

/** Minimal DTO for public store header (name + logo only). */
data class BusinessStoreDto(
  val name: String,
  val imageUrl: String?,
)
