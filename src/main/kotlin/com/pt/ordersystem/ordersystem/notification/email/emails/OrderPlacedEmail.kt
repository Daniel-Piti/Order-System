package com.pt.ordersystem.ordersystem.notification.email.emails

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationFailureReason
import org.springframework.http.HttpStatus

object OrderPlacedEmail {
  private const val TEMPLATE_NAME = "order-placed.html"

  fun buildSubject(order: OrderDto): String = "Order Placed - Order #${order.id}"

  fun validate(order: OrderDto) {
    if (order.status != OrderStatus.PLACED) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_PLACED.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_PLACED.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun buildHtml(order: OrderDto): String {
    val template = EmailTemplateHelper.loadTemplate(TEMPLATE_NAME)
    val data = mapOf(
      "customerName" to EmailTemplateHelper.escapeHtml(EmailTemplateHelper.getCustomerName(order)),
      "orderId" to order.id,
      "placedAt" to EmailTemplateHelper.formatDateTime(order.placedAt!!),
      "formattedTotal" to EmailTemplateHelper.formatCurrency(order.totalPrice),
      "productList" to EmailTemplateHelper.buildProductList(order, "#4338ca", "#c7d2fe")
    )
    return EmailTemplateHelper.replacePlaceholders(template, data)
  }
}

