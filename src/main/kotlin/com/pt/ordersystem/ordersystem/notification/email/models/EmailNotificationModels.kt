package com.pt.ordersystem.ordersystem.notification.email.models

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailOrderPlacedNotificationRequest(
  @field:NotBlank
  val orderId: String,

  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailOrderDoneNotificationRequest(
  @field:NotBlank
  val orderId: String,

  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailOrderCancelledNotificationRequest(
  @field:NotBlank
  val orderId: String,

  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailOrderUpdatedNotificationRequest(
  @field:NotBlank
  val orderId: String,

  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailNotificationResponse(
  val success: Boolean,
  val message: String,
)

