package com.pt.ordersystem.ordersystem.notification.email.emails

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationFailureReason
import org.springframework.http.HttpStatus

object OrderCancelledEmail {
  private const val TEMPLATE_NAME = "order-cancelled.html"

  fun buildSubject(order: OrderDto): String = "Order Cancelled - Order #${order.id}"

  fun validate(order: OrderDto) {
    if (order.status != OrderStatus.CANCELLED) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_CANCELLED.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_CANCELLED.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun buildHtml(order: OrderDto): String {
    val template = EmailTemplateHelper.loadTemplate(TEMPLATE_NAME)
    val data = mapOf(
      "customerName" to EmailTemplateHelper.escapeHtml(EmailTemplateHelper.getCustomerName(order)),
      "orderId" to order.id,
      "formattedTotal" to EmailTemplateHelper.formatCurrency(order.totalPrice),
      "productList" to EmailTemplateHelper.buildProductList(order, "#b91c1c", "#fca5a5")
    )
    return EmailTemplateHelper.replacePlaceholders(template, data)
  }
}

