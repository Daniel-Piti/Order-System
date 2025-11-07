package com.pt.ordersystem.ordersystem.notification.email

import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationFailureReason
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationResponse
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderCancelledNotificationRequest
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderDoneNotificationRequest
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderPlacedNotificationRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class EmailNotificationService(
  private val orderService: OrderService,
  private val emailNotificationMailer: EmailNotificationMailer,
) {

  fun sendOrderPlacedNotification(request: EmailOrderPlacedNotificationRequest): EmailNotificationResponse {
    val order = orderService.getOrderByIdInternal(request.orderId)

    if (order.status != OrderStatus.PLACED) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_PLACED.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_PLACED.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }

    emailNotificationMailer.sendOrderPlacedEmail(order, request.recipientEmail)

    return EmailNotificationResponse(
      success = true,
      message = "Order placed notification sent",
    )
  }

  fun sendOrderDoneNotification(request: EmailOrderDoneNotificationRequest): EmailNotificationResponse {
    val order = orderService.getOrderByIdInternal(request.orderId)

    if (order.status != OrderStatus.DONE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_DONE.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_DONE.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }

    emailNotificationMailer.sendOrderDoneEmail(order, request.recipientEmail)

    return EmailNotificationResponse(
      success = true,
      message = "Order done notification sent",
    )
  }

  fun sendOrderCancelledNotification(request: EmailOrderCancelledNotificationRequest): EmailNotificationResponse {
    val order = orderService.getOrderByIdInternal(request.orderId)

    if (order.status != OrderStatus.CANCELLED) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_CANCELLED.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_CANCELLED.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }

    emailNotificationMailer.sendOrderCancelledEmail(order, request.recipientEmail)

    return EmailNotificationResponse(
      success = true,
      message = "Order cancelled notification sent",
    )
  }
}
