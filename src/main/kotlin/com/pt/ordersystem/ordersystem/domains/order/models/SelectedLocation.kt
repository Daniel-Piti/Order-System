package com.pt.ordersystem.ordersystem.domains.order.models

/**
 * Selected location stored on the order.
 * JSON shape in DB: { id, name, streetAddress, city, phoneNumber }
 */
data class SelectedLocation(
  val locationId: Long?,
  val name: String?,
  val streetAddress: String?,
  val city: String?,
  val phoneNumber: String?,
)
