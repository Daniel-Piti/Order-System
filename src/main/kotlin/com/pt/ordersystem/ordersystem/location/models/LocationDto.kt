package com.pt.ordersystem.ordersystem.location.models

data class LocationDto(
  val id: String,
  val userId: String,
  val name: String,
  val address: String
)

fun LocationDbEntity.toDto() = LocationDto(
  id = id,
  userId = userId,
  name = name,
  address = address
)