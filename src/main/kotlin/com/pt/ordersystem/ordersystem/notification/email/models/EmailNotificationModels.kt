package com.pt.ordersystem.ordersystem.notification.email.models

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EmailOrderPlacedNotificationRequest(
  @field:NotNull
  val orderId: Long,
  
  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailOrderDoneNotificationRequest(
  @field:NotNull
  val orderId: Long,
  
  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailOrderCancelledNotificationRequest(
  @field:NotNull
  val orderId: Long,
  
  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

data class EmailOrderUpdatedNotificationRequest(
  @field:NotNull
  val orderId: Long,
  
  @field:NotBlank
  @field:Email
  val recipientEmail: String,
)

