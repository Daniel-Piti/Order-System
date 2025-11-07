package com.pt.ordersystem.ordersystem.notification.email.models

enum class EmailNotificationFailureReason(val userMessage: String, val technical: String) {
  ORDER_EMAIL_NOT_AVAILABLE(
    userMessage = "Unable to send order notification",
    technical = "Order notification failed - recipient email missing | "
  ),
  ORDER_NOT_PLACED(
    userMessage = "Notification can only be sent for placed orders",
    technical = "Order notification failed - order not in PLACED status | "
  ),
}
