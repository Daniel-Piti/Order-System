package com.pt.ordersystem.ordersystem.location

data class LocationDto(
  val id: String,
  val userId: String,
  val name: String,
  val address: String
)

data class NewLocationRequest(
  val userId: String,
  val name: String,
  val address: String
)

data class UpdateLocationRequest(
  val name: String,
  val address: String
)

fun LocationDbEntity.toDto() = LocationDto(
  id = id,
  userId = userId,
  name = name,
  address = address
)