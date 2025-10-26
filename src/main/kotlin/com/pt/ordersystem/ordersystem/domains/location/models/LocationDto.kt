package com.pt.ordersystem.ordersystem.domains.location.models

data class LocationDto(
  val id: String,
  val userId: String,
  val name: String,
  val streetAddress: String,
  val city: String,
  val phoneNumber: String,
)

fun LocationDbEntity.toDto() = LocationDto(
  id = id,
  userId = userId,
  name = name,
  streetAddress = streetAddress,
  city = city,
  phoneNumber = phoneNumber
)